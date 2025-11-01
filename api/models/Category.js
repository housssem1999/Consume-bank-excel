const mongoose = require('mongoose');

const categorySchema = new mongoose.Schema({
  name: {
    type: String,
    required: true
  },
  description: {
    type: String
  },
  color: {
    type: String
  },
  monthlyBudget: {
    type: Number,
    default: 0
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    default: null // null for system default categories
  }
}, {
  timestamps: true
});

// Compound index for unique name per user (or global if user is null)
categorySchema.index({ name: 1, user: 1 }, { unique: true });

// Virtual to check if this is a system category
categorySchema.virtual('isSystemCategory').get(function() {
  return this.user === null;
});

categorySchema.set('toJSON', { virtuals: true });
categorySchema.set('toObject', { virtuals: true });

module.exports = mongoose.models.Category || mongoose.model('Category', categorySchema);
