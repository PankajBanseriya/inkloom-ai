import { apiClient } from './apiClient';

export const analyticsService = {
  async getWorkspaceAnalytics(brandId) {
    const response = await apiClient.get(`/brands/${encodeURIComponent(brandId)}/analytics`);
    return response.data;
  },
};

export default analyticsService;
