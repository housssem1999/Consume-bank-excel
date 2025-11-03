const { connectToDatabase } = require('../lib/server/db');
const { authenticateUser } = require('../lib/server/middleware/auth');
const Category = require('../lib/server/models/Category');
const UserCategoryBudget = require('../lib/server/models/UserCategoryBudget');

module.exports = async (req, res) => {
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  try {
    await connectToDatabase();
    const user = await authenticateUser(req);

    // Extract category ID from URL path for PUT/DELETE operations
    const urlParts = req.url.split('?')[0].split('/');
    const categoryId = urlParts[urlParts.length - 1] !== 'categories' ? urlParts[urlParts.length - 1] : null;

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

    } else if (req.method === 'PUT') {
      // Update existing category
      if (!categoryId) {
        return res.status(400).json({
          success: false,
          message: 'Category ID is required'
        });
      }

      const { name, description, color, monthlyBudget } = req.body;

      // Find the category
      const category = await Category.findOne({
        _id: categoryId,
        user: user._id
      });

      if (!category) {
        return res.status(404).json({
          success: false,
          message: 'Category not found or you do not have permission to update it'
        });
      }

      // Check if name is being changed and if it conflicts
      if (name && name !== category.name) {
        const existing = await Category.findOne({
          name,
          _id: { $ne: categoryId },
          $or: [{ user: user._id }, { user: null }]
        });

        if (existing) {
          return res.status(400).json({
            success: false,
            message: 'Category name already exists'
          });
        }
        category.name = name;
      }

      // Update fields
      if (description !== undefined) category.description = description;
      if (color !== undefined) category.color = color;
      if (monthlyBudget !== undefined) category.monthlyBudget = monthlyBudget;

      await category.save();

      return res.status(200).json({
        success: true,
        message: 'Category updated successfully',
        category: {
          id: category._id,
          name: category.name,
          description: category.description,
          color: category.color,
          monthlyBudget: category.monthlyBudget
        }
      });

    } else if (req.method === 'DELETE') {
      // Delete category
      if (!categoryId) {
        return res.status(400).json({
          success: false,
          message: 'Category ID is required'
        });
      }

      const category = await Category.findOne({
        _id: categoryId,
        user: user._id
      });

      if (!category) {
        return res.status(404).json({
          success: false,
          message: 'Category not found or you do not have permission to delete it'
        });
      }

      // Check if category is being used by transactions
      const Transaction = require('../lib/server/models/Transaction');
      const transactionCount = await Transaction.countDocuments({
        category: categoryId,
        user: user._id
      });

      if (transactionCount > 0) {
        return res.status(400).json({
          success: false,
          message: `Cannot delete category. It is used by ${transactionCount} transaction(s).`
        });
      }

      // Delete associated budgets
      await UserCategoryBudget.deleteMany({
        category: categoryId,
        user: user._id
      });

      // Delete the category
      await Category.deleteOne({ _id: categoryId });

      return res.status(200).json({
        success: true,
        message: 'Category deleted successfully'
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
