const bcrypt = require('bcryptjs');
const { connectToDatabase } = require('../db');
const User = require('../models/User');
const Category = require('../models/Category');
const { generateToken } = require('../utils/jwt');

const DEFAULT_CATEGORIES = [
  { name: 'Food & Dining', description: 'Groceries, restaurants, and food expenses', color: '#FF6384' },
  { name: 'Transportation', description: 'Gas, public transit, and travel', color: '#36A2EB' },
  { name: 'Shopping', description: 'Clothing, electronics, and general shopping', color: '#FFCE56' },
  { name: 'Bills & Utilities', description: 'Electricity, water, internet, and phone bills', color: '#4BC0C0' },
  { name: 'Entertainment', description: 'Movies, games, and leisure activities', color: '#9966FF' },
  { name: 'Healthcare', description: 'Medical expenses and pharmacy', color: '#FF9F40' },
  { name: 'Income', description: 'Salary, freelance, and other income', color: '#4CAF50' },
  { name: 'Transfer', description: 'Money transfers and ATM withdrawals', color: '#757575' },
  { name: 'Housing', description: 'Rent, mortgage, and property expenses', color: '#795548' },
  { name: 'Insurance', description: 'Health, car, and life insurance', color: '#607D8B' },
  { name: 'Other', description: 'Miscellaneous expenses', color: '#9E9E9E' }
];

module.exports = async (req, res) => {
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    await connectToDatabase();
    const { username, email, password, firstName, lastName } = req.body;

    if (!username || !email || !password) {
      return res.status(400).json({
        success: false,
        message: 'Username, email, and password are required'
      });
    }

    if (username.length < 3 || username.length > 50) {
      return res.status(400).json({
        success: false,
        message: 'Username must be between 3 and 50 characters'
      });
    }

    if (password.length < 6) {
      return res.status(400).json({
        success: false,
        message: 'Password must be at least 6 characters'
      });
    }

    const existingUsername = await User.findOne({ username });
    if (existingUsername) {
      return res.status(400).json({
        success: false,
        message: 'Username already exists'
      });
    }

    const existingEmail = await User.findOne({ email: email.toLowerCase() });
    if (existingEmail) {
      return res.status(400).json({
        success: false,
        message: 'Email already exists'
      });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const user = new User({
      username,
      email: email.toLowerCase(),
      password: hashedPassword,
      firstName,
      lastName
    });

    await user.save();

    // Initialize default categories
    for (const cat of DEFAULT_CATEGORIES) {
      const existingCategory = await Category.findOne({ name: cat.name, user: null });
      if (!existingCategory) {
        await Category.create({ ...cat, user: null });
      }
    }

    const token = generateToken(user);
    const userResponse = {
      id: user._id,
      username: user.username,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      fullName: user.fullName,
      role: user.role,
      createdAt: user.createdAt,
      lastLoginAt: user.lastLoginAt
    };

    return res.status(201).json({
      success: true,
      message: 'Registration successful',
      token,
      user: userResponse
    });

  } catch (error) {
    console.error('Registration error:', error);
    return res.status(500).json({
      success: false,
      message: 'Registration failed: ' + error.message
    });
  }
};
