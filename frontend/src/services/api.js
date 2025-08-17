import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Dashboard API
export const dashboardAPI = {
  getFinancialSummary: (startDate, endDate) => {
    const params = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    return api.get('/dashboard/summary', { params });
  },
  
  getCurrentMonthSummary: () => api.get('/dashboard/summary/current-month'),
  
  getCurrentYearSummary: () => api.get('/dashboard/summary/current-year'),
  
  getTransactions: (startDate, endDate, page = 0, size = 50, sortBy = null, sortDir = 'desc') => {
    const params = { page, size };
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    if (sortBy) {
      params.sortBy = sortBy;
      params.sortDir = sortDir;
    }
    return api.get('/dashboard/transactions', { params });
  },
  
  getTopExpenseCategories: (limit = 5) => 
    api.get('/dashboard/top-expenses', { params: { limit } }),
  
  getAverageMonthlyExpenses: (months = 12) => 
    api.get('/dashboard/average-monthly-expenses', { params: { months } }),
  
  getQuickStats: () => api.get('/dashboard/stats'),
};

// File Upload API
export const uploadAPI = {
  uploadExcelFile: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/upload/excel', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  
  getSampleFormat: () => api.get('/upload/sample-format'),
};

// Categories API
export const categoriesAPI = {
  getAllCategories: () => api.get('/categories'),
  
  getCategoryById: (id) => api.get(`/categories/${id}`),
  
  createCategory: (categoryData) => api.post('/categories', categoryData),
};

// Utility functions
export const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '$0.00';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
  }).format(amount);
};

export const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

export const getAmountColor = (amount) => {
  if (amount > 0) return '#52c41a'; // green for positive
  if (amount < 0) return '#ff4d4f'; // red for negative
  return '#1890ff'; // blue for zero
};

export default api;
