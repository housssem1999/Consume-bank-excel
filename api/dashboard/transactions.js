const { connectToDatabase } = require('../db');
const { authenticateUser } = require('../middleware/auth');
const Transaction = require('../models/Transaction');

module.exports = async (req, res) => {
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  if (req.method !== 'GET') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    await connectToDatabase();
    const user = await authenticateUser(req);

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

  } catch (error) {
    console.error('Get transactions error:', error);
    return res.status(500).json({
      success: false,
      message: error.message || 'Internal server error'
    });
  }
};
