import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
const AI_SERVICE_URL = process.env.REACT_APP_AI_SERVICE_URL || 'http://localhost:8000';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle auth errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Trade API
export const tradeAPI = {
  getAll: () => api.get('/trades'),
  getById: (id) => api.get(`/trades/${id}`),
  create: (data) => api.post('/trades', data),
  update: (id, data) => api.put(`/trades/${id}`, data),
  delete: (id) => api.delete(`/trades/${id}`),
  getPortfolio: () => api.get('/trades/portfolio'),
};

// Expense API
export const expenseAPI = {
  getAll: () => api.get('/expenses'),
  getById: (id) => api.get(`/expenses/${id}`),
  create: (data) => api.post('/expenses', data),
  update: (id, data) => api.put(`/expenses/${id}`, data),
  delete: (id) => api.delete(`/expenses/${id}`),
  getSummary: () => api.get('/expenses/summary'),
  getByCategory: (category) => api.get(`/expenses/category/${category}`),
};

// Goal API
export const goalAPI = {
  getAll: () => api.get('/goals'),
  getById: (id) => api.get(`/goals/${id}`),
  create: (data) => api.post('/goals', data),
  update: (id, data) => api.put(`/goals/${id}`, data),
  delete: (id) => api.delete(`/goals/${id}`),
  getActive: () => api.get('/goals/active'),
  updateProgress: (id, data) => api.put(`/goals/${id}/progress`, data),
};

// AI Service API
export const aiAPI = {
  predictPrice: (data) => axios.post(`${AI_SERVICE_URL}/predict-price`, data),
  getRecommendations: (data) => axios.post(`${AI_SERVICE_URL}/get-recommendations`, data),
  analyzeMarket: (data) => axios.post(`${AI_SERVICE_URL}/analyze-market`, data),
  healthCheck: () => axios.get(`${AI_SERVICE_URL}/health`),
};

// Market Data API
export const marketAPI = {
  getCurrentPrice: (symbol) => api.get(`/market/price/${symbol}`),
  getQuote: (symbol) => api.get(`/market/quote/${symbol}`),
};

export default api; 