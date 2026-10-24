import { apiClient } from './apiClient';

export const claimsService = {
  async getClaims(brandId) {
    const response = await apiClient.get(`/brands/${encodeURIComponent(brandId)}/claims`);
    return response.data || [];
  },

  async getClaimById(brandId, claimId) {
    const response = await apiClient.get(
      `/brands/${encodeURIComponent(brandId)}/claims/${encodeURIComponent(claimId)}`
    );
    return response.data;
  },

  async createClaim(brandId, claimData) {
    const response = await apiClient.post(
      `/brands/${encodeURIComponent(brandId)}/claims`,
      claimData
    );
    return response.data;
  },

  async updateClaim(brandId, claimId, claimData) {
    const response = await apiClient.put(
      `/brands/${encodeURIComponent(brandId)}/claims/${encodeURIComponent(claimId)}`,
      claimData
    );
    return response.data;
  },

  async deleteClaim(brandId, claimId) {
    const response = await apiClient.delete(
      `/brands/${encodeURIComponent(brandId)}/claims/${encodeURIComponent(claimId)}`
    );
    return response.data;
  },

  async updateClaimStatus(brandId, claimId, status) {
    const response = await apiClient.request(
      `/brands/${encodeURIComponent(brandId)}/claims/${encodeURIComponent(claimId)}/status?status=${encodeURIComponent(status)}`,
      { method: 'PATCH' }
    );
    return response.data;
  },
};

export default claimsService;
