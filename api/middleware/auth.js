const { verifyToken } = require('../utils/jwt');
const User = require('../models/User');

async function authenticateUser(req) {
  const authHeader = req.headers.authorization || req.headers.Authorization;
  
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    throw new Error('No token provided');
  }

  const token = authHeader.substring(7); // Remove 'Bearer ' prefix
  const decoded = verifyToken(token);

  if (!decoded) {
    throw new Error('Invalid or expired token');
  }

  // Get user from database
  const user = await User.findById(decoded.id);
  
  if (!user || !user.enabled) {
    throw new Error('User not found or disabled');
  }

  return user;
}

function corsHeaders(origin = '*') {
  return {
    'Access-Control-Allow-Origin': origin,
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    'Access-Control-Max-Age': '86400',
  };
}

module.exports = {
  authenticateUser,
  corsHeaders
};
