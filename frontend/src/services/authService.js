import { apiClient } from './apiClient';

export const authService = {
  /**
   * Authenticate user with email and password
   * @param {string} email
   * @param {string} password
   * @returns {Promise<{ token: string, user: Object }>}
   */
  async login(email, password) {
    const response = await apiClient.post('/auth/login', { email, password });
    if (response && response.data && response.data.token) {
      apiClient.setToken(response.data.token);
      return response.data;
    }
    throw new Error(response?.message || 'Authentication failed');
  },

  /**
   * Register a new user account
   * @param {Object} data - { email, password, fullName, role, workspaceName }
   * @returns {Promise<{ token: string, user: Object }>}
   */
  async register(data) {
    const response = await apiClient.post('/auth/register', data);
    if (response && response.data && response.data.token) {
      apiClient.setToken(response.data.token);
      return response.data;
    }
    throw new Error(response?.message || 'Registration failed');
  },

  /**
   * Fetch currently authenticated user profile
   * @returns {Promise<Object>}
   */
  async getMe() {
    const response = await apiClient.get('/auth/me');
    return response?.data;
  },

  /**
   * Clear client-side session tokens
   */
  logout() {
    apiClient.setToken(null);
  },
};
