import api from './api';

export const authAPI = {
  login: (credentials) => {
    return api.post('/auth/signin', credentials);
  },

  register: (userData) => {
    return api.post('/auth/signup', userData);
  },

  logout: () => {
    return api.post('/auth/signout');
  },

  getCurrentUser: () => {
    return api.get('/auth/me');
  },

  refreshToken: () => {
    // For future implementation if refresh tokens are needed
    return api.post('/auth/refresh');
  }
};