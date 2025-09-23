import api from './api';

export const authAPI = {
  login: (credentials) => {
    // Backend: /api/auth/login
    return api.post('/auth/login', credentials);
  },

  register: (userData) => {
    // Backend: /api/auth/register
    return api.post('/auth/register', userData);
  },

  logout: () => {
    // No explicit logout endpoint in backend, just remove token client-side
    localStorage.removeItem('token');
    return Promise.resolve();
  },

  getCurrentUser: () => {
    // Backend: /api/auth/me
    return api.get('/auth/me');
  },

  // No refresh endpoint implemented in backend
};