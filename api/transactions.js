const { connectToDatabase } = require('../lib/server/db');
const { authenticateUser } = require('../lib/server/middleware/auth');
const Transaction = require('../lib/server/models/Transaction');
const Category = require('../lib/server/models/Category');
const { categorizeByDescription } = require('../lib/server/utils/categorizer');

module.exports = async (req, res) => {
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  try {
    await connectToDatabase();
    const user = await authenticateUser(req);
    
    // Extract ID from URL if present (e.g., /api/transactions/123)
    const urlParts = req.url.split('?')[0].split('/');
    const id = urlParts[urlParts.length - 1] !== 'transactions' ? urlParts[urlParts.length - 1] : null;

    // POST /api/transactions - Create transaction
    if (!id && req.method === 'POST') {
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
    }

    // GET /api/transactions/:id - Get transaction by ID
    if (id && req.method === 'GET') {
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
    }

    // PUT /api/transactions/:id - Update transaction
    if (id && req.method === 'PUT') {
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
    }

    // DELETE /api/transactions/:id - Delete transaction
    if (id && req.method === 'DELETE') {
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
    }

    return res.status(405).json({ success: false, message: 'Method not allowed' });

  } catch (error) {
    console.error('Transaction error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Internal server error'
    });
  }
};
