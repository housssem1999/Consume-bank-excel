const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'mySecretKeyThatIsAtLeast32CharactersLongForHS256Algorithm';
const JWT_EXPIRATION = process.env.JWT_EXPIRATION || '24h';

function generateToken(user) {
  const payload = {
    id: user._id || user.id,
    username: user.username,
    email: user.email,
    role: user.role
  };

  return jwt.sign(payload, JWT_SECRET, {
    expiresIn: JWT_EXPIRATION
  });
}

function verifyToken(token) {
  try {
    return jwt.verify(token, JWT_SECRET);
  } catch (error) {
    return null;
  }
}

module.exports = {
  generateToken,
  verifyToken
};
