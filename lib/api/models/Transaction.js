const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema({
  date: {
    type: Date,
    required: true
  },
  description: {
    type: String,
    required: true,
    maxlength: 500
  },
  amount: {
    type: Number,
    required: true
  },
  type: {
    type: String,
    enum: ['INCOME', 'EXPENSE', 'TRANSFER'],
    required: true
  },
  category: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Category'
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  reference: {
    type: String
  }
}, {
  timestamps: true
});

// Indexes for efficient querying
transactionSchema.index({ user: 1, date: -1 });
transactionSchema.index({ user: 1, category: 1 });
transactionSchema.index({ user: 1, type: 1 });

module.exports = mongoose.models.Transaction || mongoose.model('Transaction', transactionSchema);
