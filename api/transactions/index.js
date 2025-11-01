const { connectToDatabase } = require('../db');
const { authenticateUser } = require('../middleware/auth');
const Transaction = require('../models/Transaction');
const Category = require('../models/Category');
const { categorizeByDescription } = require('../utils/categorizer');

module.exports = async (req, res) => {
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  try {
    await connectToDatabase();
    const user = await authenticateUser(req);

    if (req.method === 'POST') {
      // Create new transaction
      const { date, description, amount, type, categoryId, reference } = req.body;

      if (!date || !description || !amount || !type) {
        return res.status(400).json({
          success: false,
          message: 'Date, description, amount, and type are required'
        });
      }

      const amountNum = parseFloat(amount);
      if (isNaN(amountNum)) {
        return res.status(400).json({
          success: false,
          message: 'Amount must be a valid number'
        });
      }

      let category = null;
      if (categoryId) {
        category = await Category.findById(categoryId);
        if (!category) {
          return res.status(400).json({
            success: false,
            message: 'Category not found'
          });
        }
      } else {
        // Auto-categorize
        category = await categorizeByDescription(description, user._id);
      }

      const transaction = new Transaction({
        date: new Date(date),
        description: description.trim(),
        amount: amountNum,
        type: type.toUpperCase(),
        category: category ? category._id : null,
        user: user._id,
        reference: reference ? reference.trim() : undefined
      });

      await transaction.save();
      await transaction.populate('category');

      return res.status(201).json({
        success: true,
        message: 'Transaction created successfully',
        transaction
      });

    } else {
      return res.status(405).json({ success: false, message: 'Method not allowed' });
    }

  } catch (error) {
    console.error('Transaction error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Internal server error'
    });
  }
};
