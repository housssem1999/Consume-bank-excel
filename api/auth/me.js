const { connectToDatabase } = require('../db');
const { authenticateUser } = require('../middleware/auth');

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
      user: userResponse
    });

  } catch (error) {
    console.error('Get current user error:', error);
    return res.status(401).json({
      success: false,
      message: error.message || 'Unauthorized'
    });
  }
};
