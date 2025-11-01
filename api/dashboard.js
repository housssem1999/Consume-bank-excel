const { connectToDatabase } = require('./lib/db');
const { authenticateUser } = require('./middleware/auth');
const Transaction = require('./models/Transaction');

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
        const categoryName = transaction.category ? transaction.category.name : 'Other';
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

      // Convert monthly trends to array
      const monthlyTrendsArray = Object.entries(monthlyTrends)
        .map(([month, data]) => ({ month, ...data }))
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
