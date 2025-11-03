const { connectToDatabase } = require('../lib/server/db');
const { authenticateUser } = require('../lib/server/middleware/auth');
const Transaction = require('../lib/server/models/Transaction');
const Category = require('../lib/server/models/Category');
const UserCategoryBudget = require('../lib/server/models/UserCategoryBudget');

module.exports = async (req, res) => {
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  if (req.method !== 'GET') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  const path = req.url.split('?')[0];

  try {
    await connectToDatabase();
    const user = await authenticateUser(req);

    // GET /api/dashboard/summary
    if (path.endsWith('/summary')) {
      const { startDate, endDate } = req.query;

      let dateFilter = { user: user._id };
      
      if (startDate && endDate) {
        dateFilter.date = {
          $gte: new Date(startDate),
          $lte: new Date(endDate)
        };
      } else {
        // Default to current year
        const now = new Date();
        dateFilter.date = {
          $gte: new Date(now.getFullYear(), 0, 1),
          $lte: new Date(now.getFullYear(), 11, 31)
        };
      }

      // Get all transactions for the period
      const transactions = await Transaction.find(dateFilter).populate('category');

      // Calculate statistics
      let totalIncome = 0;
      let totalExpenses = 0;
      const categoryBreakdown = {};
      const monthlyTrends = {};

      for (const transaction of transactions) {
        if (transaction.type === 'INCOME') {
          totalIncome += transaction.amount;
        } else if (transaction.type === 'EXPENSE') {
          totalExpenses += transaction.amount;
        }

        // Category breakdown
        const categoryName = transaction.category ? transaction.category.name : 'Uncategorized';
        if (!categoryBreakdown[categoryName]) {
          categoryBreakdown[categoryName] = 0;
        }
        if (transaction.type === 'EXPENSE') {
          categoryBreakdown[categoryName] += transaction.amount;
        }

        // Monthly trends
        const monthKey = `${transaction.date.getFullYear()}-${String(transaction.date.getMonth() + 1).padStart(2, '0')}`;
        if (!monthlyTrends[monthKey]) {
          monthlyTrends[monthKey] = { income: 0, expenses: 0 };
        }
        if (transaction.type === 'INCOME') {
          monthlyTrends[monthKey].income += transaction.amount;
        } else if (transaction.type === 'EXPENSE') {
          monthlyTrends[monthKey].expenses += transaction.amount;
        }
      }

      // Convert category breakdown to array
      const categoryBreakdownArray = Object.entries(categoryBreakdown)
        .map(([name, amount]) => ({ name, amount }))
        .sort((a, b) => b.amount - a.amount);

      // Convert monthly trends to array with proper format for SavingsRateChart
      const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
      ];

      const monthlyTrendsArray = Object.entries(monthlyTrends)
        .map(([monthKey, data]) => {
          const [year, month] = monthKey.split('-').map(Number);
          return {
            month: monthKey,
            year,
            monthNum: month,
            monthName: monthNames[month - 1],
            income: data.income,
            expenses: data.expenses,
            netAmount: data.income - data.expenses
          };
        })
        .sort((a, b) => a.month.localeCompare(b.month));

      return res.status(200).json({
        totalIncome,
        totalExpenses,
        netIncome: totalIncome - totalExpenses,
        totalTransactions: transactions.length,
        categoryBreakdown: categoryBreakdownArray,
        monthlyTrends: monthlyTrendsArray
      });
    }

    // GET /api/dashboard/transactions
    if (path.endsWith('/transactions')) {
      const { startDate, endDate, page = 0, size = 50, sortBy = 'date', sortDir = 'desc' } = req.query;

      let filter = { user: user._id };
      
      if (startDate && endDate) {
        filter.date = {
          $gte: new Date(startDate),
          $lte: new Date(endDate)
        };
      }

      const sortOrder = sortDir === 'asc' ? 1 : -1;
      const sortField = {};
      sortField[sortBy] = sortOrder;

      const skip = parseInt(page) * parseInt(size);
      const limit = parseInt(size);

      const [transactions, total] = await Promise.all([
        Transaction.find(filter)
          .populate('category')
          .sort(sortField)
          .skip(skip)
          .limit(limit),
        Transaction.countDocuments(filter)
      ]);

      const totalPages = Math.ceil(total / limit);

      return res.status(200).json({
        data: transactions,
        total,
        page: parseInt(page),
        size: limit,
        totalPages
      });
    }

    // GET /api/dashboard/budget-comparison/period
    if (path.includes('/budget-comparison/period')) {
      const { startDate, endDate } = req.query;

      if (!startDate || !endDate) {
        return res.status(400).json({
          success: false,
          message: 'startDate and endDate are required'
        });
      }

      const start = new Date(startDate);
      const end = new Date(endDate);

      // Get all categories available to the user (system categories + user's custom categories)
      const categories = await Category.find({
        $or: [
          { user: null }, // System categories
          { user: user._id } // User's custom categories
        ]
      });

      // Get actual spending for the period by category
      const spendingAggregation = await Transaction.aggregate([
        {
          $match: {
            user: user._id,
            type: 'EXPENSE',
            date: { $gte: start, $lte: end }
          }
        },
        {
          $lookup: {
            from: 'categories',
            localField: 'category',
            foreignField: '_id',
            as: 'categoryData'
          }
        },
        {
          $unwind: {
            path: '$categoryData',
            preserveNullAndEmptyArrays: true
          }
        },
        {
          $group: {
            _id: '$categoryData._id',
            categoryName: { $first: '$categoryData.name' },
            actualAmount: { $sum: { $abs: '$amount' } }
          }
        }
      ]);

      // Create a map for easy lookup
      const spendingMap = {};
      spendingAggregation.forEach(item => {
        if (item.categoryName) {
          spendingMap[item.categoryName] = item.actualAmount;
        }
      });

      // Get user budgets for the period
      const startYear = start.getFullYear();
      const startMonth = start.getMonth() + 1;
      const endYear = end.getFullYear();
      const endMonth = end.getMonth() + 1;

      // Build budget query conditions based on date range
      const budgetQuery = { user: user._id };
      
      if (startYear === endYear) {
        // Same year: simple month range
        budgetQuery.year = startYear;
        budgetQuery.month = { $gte: startMonth, $lte: endMonth };
      } else {
        // Multiple years: combine conditions
        // Note: For consecutive years (e.g., 2023-2024), the middle condition matches nothing, which is correct
        budgetQuery.$or = [
          { year: startYear, month: { $gte: startMonth } }, // Months from start year
          { year: { $gt: startYear, $lt: endYear } }, // Full years in between (if any exist)
          { year: endYear, month: { $lte: endMonth } } // Months in end year
        ];
      }

      const budgets = await UserCategoryBudget.find(budgetQuery).populate('category');

      // Aggregate budgets by category
      const categoryBudgets = {};
      budgets.forEach(budget => {
        const categoryName = budget.category.name;
        if (!categoryBudgets[categoryName]) {
          categoryBudgets[categoryName] = {
            totalBudget: 0,
            color: budget.category.color
          };
        }
        categoryBudgets[categoryName].totalBudget += budget.monthlyBudget;
      });

      // Build budget comparison response
      const budgetComparisons = [];
      
      for (const category of categories) {
        const categoryName = category.name;
        const budgetInfo = categoryBudgets[categoryName];
        
        if (budgetInfo && budgetInfo.totalBudget > 0) {
          const budgetAmount = budgetInfo.totalBudget;
          const actualAmount = spendingMap[categoryName] || 0;
          
          // Calculate percentage difference
          const difference = actualAmount - budgetAmount;
          const percentageDifference = budgetAmount > 0 ? (difference / budgetAmount) * 100 : 0;
          
          // Determine status color
          let statusColor;
          if (actualAmount <= budgetAmount) {
            statusColor = '#52c41a'; // Green - under budget
          } else {
            statusColor = '#ff4d4f'; // Red - over budget
          }
          
          budgetComparisons.push({
            categoryName,
            budgetAmount,
            actualAmount,
            percentageDifference,
            statusColor,
            categoryColor: budgetInfo.color || category.color
          });
        }
      }

      return res.status(200).json(budgetComparisons);
    }

    // GET /api/dashboard/expense-heatmap
    if (path.includes('/expense-heatmap')) {
      const { startDate, endDate } = req.query;

      let dateFilter = { user: user._id, type: 'EXPENSE' };
      
      if (startDate && endDate) {
        dateFilter.date = {
          $gte: new Date(startDate),
          $lte: new Date(endDate)
        };
      } else {
        // Default to last year
        const now = new Date();
        const oneYearAgo = new Date(now);
        oneYearAgo.setFullYear(now.getFullYear() - 1);
        dateFilter.date = {
          $gte: oneYearAgo,
          $lte: now
        };
      }

      // Aggregate expenses by category and day of week
      const heatmapData = await Transaction.aggregate([
        {
          $match: dateFilter
        },
        {
          $lookup: {
            from: 'categories',
            localField: 'category',
            foreignField: '_id',
            as: 'categoryData'
          }
        },
        {
          $unwind: {
            path: '$categoryData',
            preserveNullAndEmptyArrays: true
          }
        },
        {
          $group: {
            _id: {
              category: '$categoryData.name',
              dayOfWeek: { $dayOfWeek: '$date' }
            },
            amount: { $sum: { $abs: '$amount' } }
          }
        },
        {
          $project: {
            _id: 0,
            category: { $ifNull: ['$_id.category', 'Uncategorized'] },
            dayOfWeek: '$_id.dayOfWeek',
            amount: 1
          }
        }
      ]);

      // Map day numbers to day names
      // Note: MongoDB $dayOfWeek returns 1=Sunday, 2=Monday, ..., 7=Saturday
      // This is consistent with the Spring Boot implementation using EXTRACT(DOW)
      const dayMap = {
        1: 'Sunday',
        2: 'Monday',
        3: 'Tuesday',
        4: 'Wednesday',
        5: 'Thursday',
        6: 'Friday',
        7: 'Saturday'
      };

      // Convert day numbers to day names
      const formattedHeatmapData = heatmapData.map(item => ({
        category: item.category,
        dayOfWeek: dayMap[item.dayOfWeek] || 'Unknown',
        amount: item.amount
      }));

      return res.status(200).json(formattedHeatmapData);
    }

    return res.status(404).json({
      success: false,
      message: 'Endpoint not found'
    });

  } catch (error) {
    console.error('Dashboard error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Internal server error'
    });
  }
};
