import { apiClient } from './apiClient';

export const healthService = {
  async checkHealth() {
    return apiClient.get('/health');
  },
};
