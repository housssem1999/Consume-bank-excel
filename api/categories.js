const { connectToDatabase } = require('./lib/db');
const { authenticateUser } = require('./middleware/auth');
const Category = require('./models/Category');
const UserCategoryBudget = require('./models/UserCategoryBudget');

module.exports = async (req, res) => {
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  try {
    await connectToDatabase();
    const user = await authenticateUser(req);

    if (req.method === 'GET') {
      // Get all categories (system + user's custom categories)
      const categories = await Category.find({
        $or: [{ user: null }, { user: user._id }]
      }).sort({ name: 1 });

      // Get user's budgets for categories
      const currentDate = new Date();
      const budgets = await UserCategoryBudget.find({
        user: user._id,
        year: currentDate.getFullYear(),
        month: currentDate.getMonth() + 1
      });

      // Map budgets to categories
      const categoriesWithBudgets = categories.map(cat => {
        const budget = budgets.find(b => b.category.toString() === cat._id.toString());
        return {
          id: cat._id,
          name: cat.name,
          description: cat.description,
          color: cat.color,
          monthlyBudget: budget ? budget.monthlyBudget : (cat.monthlyBudget || 0),
          isSystemCategory: cat.user === null
        };
      });

      return res.status(200).json(categoriesWithBudgets);

    } else if (req.method === 'POST') {
      // Create new category
      const { name, description, color, monthlyBudget } = req.body;

      if (!name) {
        return res.status(400).json({
          success: false,
          message: 'Category name is required'
        });
      }

      // Check if category already exists for this user
      const existing = await Category.findOne({
        name,
        $or: [{ user: user._id }, { user: null }]
      });

      if (existing) {
        return res.status(400).json({
          success: false,
          message: 'Category already exists'
        });
      }

      const category = new Category({
        name,
        description,
        color: color || '#9E9E9E',
        monthlyBudget: monthlyBudget || 0,
        user: user._id
      });

      await category.save();

      return res.status(201).json({
        success: true,
        message: 'Category created successfully',
        category: {
          id: category._id,
          name: category.name,
          description: category.description,
          color: category.color,
          monthlyBudget: category.monthlyBudget
        }
      });

    } else {
      return res.status(405).json({ success: false, message: 'Method not allowed' });
    }

  } catch (error) {
    console.error('Categories error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Internal server error'
    });
  }
};
