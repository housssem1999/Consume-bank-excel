const mongoose = require('mongoose');

const userCategoryBudgetSchema = new mongoose.Schema({
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  category: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Category',
    required: true
  },
  monthlyBudget: {
    type: Number,
    required: true,
    default: 0
  },
  year: {
    type: Number,
    required: true
  },
  month: {
    type: Number,
    required: true,
    min: 1,
    max: 12
  }
}, {
  timestamps: true
});

// Compound index for unique budget per user per category per month
userCategoryBudgetSchema.index({ user: 1, category: 1, year: 1, month: 1 }, { unique: true });

module.exports = mongoose.models.UserCategoryBudget || mongoose.model('UserCategoryBudget', userCategoryBudgetSchema);
