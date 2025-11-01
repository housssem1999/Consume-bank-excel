const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  username: {
    type: String,
    required: true,
    unique: true,
    minlength: 3,
    maxlength: 50
  },
  email: {
    type: String,
    required: true,
    unique: true,
    lowercase: true,
    match: /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  },
  password: {
    type: String,
    required: true,
    minlength: 6
  },
  firstName: {
    type: String,
    maxlength: 100
  },
  lastName: {
    type: String,
    maxlength: 100
  },
  enabled: {
    type: Boolean,
    default: true
  },
  accountNonExpired: {
    type: Boolean,
    default: true
  },
  accountNonLocked: {
    type: Boolean,
    default: true
  },
  credentialsNonExpired: {
    type: Boolean,
    default: true
  },
  role: {
    type: String,
    enum: ['USER', 'ADMIN'],
    default: 'USER'
  },
  createdAt: {
    type: Date,
    default: Date.now
  },
  lastLoginAt: {
    type: Date
  }
}, {
  timestamps: true
});

// Virtual for full name
userSchema.virtual('fullName').get(function() {
  if (this.firstName && this.lastName) {
    return `${this.firstName} ${this.lastName}`;
  } else if (this.firstName) {
    return this.firstName;
  } else if (this.lastName) {
    return this.lastName;
  }
  return this.username;
});

// Ensure virtuals are included in JSON
userSchema.set('toJSON', { virtuals: true });
userSchema.set('toObject', { virtuals: true });

module.exports = mongoose.models.User || mongoose.model('User', userSchema);
