const { connectToDatabase } = require('../db');
const { authenticateUser } = require('../middleware/auth');
const Transaction = require('../models/Transaction');
const Category = require('../models/Category');

module.exports = async (req, res) => {
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  try {
    await connectToDatabase();
    const user = await authenticateUser(req);
    
    const { id } = req.query;

    if (req.method === 'GET') {
      const transaction = await Transaction.findOne({ _id: id, user: user._id }).populate('category');
      
      if (!transaction) {
        return res.status(404).json({
          success: false,
          message: 'Transaction not found'
        });
      }

      return res.status(200).json({
        success: true,
        transaction
      });

    } else if (req.method === 'PUT') {
      const transaction = await Transaction.findOne({ _id: id, user: user._id });
      
      if (!transaction) {
        return res.status(404).json({
          success: false,
          message: 'Transaction not found'
        });
      }

      const { date, description, amount, type, categoryId, reference } = req.body;

      if (date) transaction.date = new Date(date);
      if (description) transaction.description = description.trim();
      if (amount) {
        const amountNum = parseFloat(amount);
        if (!isNaN(amountNum)) transaction.amount = amountNum;
      }
      if (type) transaction.type = type.toUpperCase();
      if (categoryId) {
        const category = await Category.findById(categoryId);
        if (category) transaction.category = category._id;
      }
      if (reference !== undefined) transaction.reference = reference ? reference.trim() : null;

      await transaction.save();
      await transaction.populate('category');

      return res.status(200).json({
        success: true,
        message: 'Transaction updated successfully',
        transaction
      });

    } else if (req.method === 'DELETE') {
      const transaction = await Transaction.findOneAndDelete({ _id: id, user: user._id });
      
      if (!transaction) {
        return res.status(404).json({
          success: false,
          message: 'Transaction not found'
        });
      }

      return res.status(200).json({
        success: true,
        message: 'Transaction deleted successfully'
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
