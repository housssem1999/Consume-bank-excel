const bcrypt = require('bcryptjs');
const { connectToDatabase } = require('../db');
const User = require('../models/User');
const { generateToken } = require('../utils/jwt');

module.exports = async (req, res) => {
  // Handle CORS preflight
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    await connectToDatabase();
    const { usernameOrEmail, password } = req.body;

    if (!usernameOrEmail || !password) {
      return res.status(400).json({
        success: false,
        message: 'Username/email and password are required'
      });
    }

    const user = await User.findOne({
      $or: [{ username: usernameOrEmail }, { email: usernameOrEmail.toLowerCase() }]
    });

    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'Invalid username/email or password'
      });
    }

    const isPasswordValid = await bcrypt.compare(password, user.password);
    if (!isPasswordValid) {
      return res.status(401).json({
        success: false,
        message: 'Invalid username/email or password'
      });
    }

    if (!user.enabled) {
      return res.status(401).json({
        success: false,
        message: 'Account is disabled'
      });
    }

    user.lastLoginAt = new Date();
    await user.save();

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

    return res.status(200).json({
      success: true,
      message: 'Login successful',
      token,
      user: userResponse
    });

  } catch (error) {
    console.error('Login error:', error);
    return res.status(500).json({
      success: false,
      message: 'Login failed. Please try again.'
    });
  }
};
