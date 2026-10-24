import { apiClient } from './apiClient';

export const brandService = {
  /**
   * Retrieves all brand workspaces available to the current user.
   */
  async getBrands() {
    const response = await apiClient.get('/brands');
    return response.data || [];
  },

  /**
   * Retrieves a specific brand workspace by ID.
   * @param {string} brandId
   */
  async getBrandById(brandId) {
    const response = await apiClient.get(`/brands/${encodeURIComponent(brandId)}`);
    return response.data;
  },

  /**
   * Creates a new brand workspace.
   * @param {{ name: string, industry: string, description: string }} payload
   */
  async createBrand(payload) {
    const response = await apiClient.post('/brands', payload);
    return response.data;
  },

  /**
   * Updates an existing brand workspace.
   * @param {string} brandId
   * @param {{ name: string, industry: string, description: string }} payload
   */
  async updateBrand(brandId, payload) {
    const response = await apiClient.put(`/brands/${encodeURIComponent(brandId)}`, payload);
    return response.data;
  },

  /**
   * Retrieves the active brand guidelines for a workspace.
   * @param {string} brandId
   */
  async getGuidelines(brandId) {
    const response = await apiClient.get(`/brands/${encodeURIComponent(brandId)}/guidelines`);
    return response.data;
  },

  /**
   * Updates brand guidelines for a workspace.
   * @param {string} brandId
   * @param {object} guidelinesData
   */
  async updateGuidelines(brandId, guidelinesData) {
    const response = await apiClient.put(
      `/brands/${encodeURIComponent(brandId)}/guidelines`,
      guidelinesData
    );
    return response.data;
  },

  /**
   * Retrieves verified ground-truth sources for a brand workspace.
   * @param {string} brandId
   */
  async getSources(brandId) {
    const response = await apiClient.get(`/brands/${encodeURIComponent(brandId)}/sources`);
    return response.data || [];
  },

  /**
   * Registers a new approved ground-truth source.
   * @param {string} brandId
   * @param {{ title: string, sourceType: string, owner: string, excerpt: string, referenceUrl?: string, version?: string }} payload
   */
  async createSource(brandId, payload) {
    const response = await apiClient.post(
      `/brands/${encodeURIComponent(brandId)}/sources`,
      payload
    );
    return response.data;
  },

  /**
   * Updates an approved source.
   * @param {string} brandId
   * @param {string} sourceId
   * @param {object} payload
   */
  async updateSource(brandId, sourceId, payload) {
    const response = await apiClient.put(
      `/brands/${encodeURIComponent(brandId)}/sources/${encodeURIComponent(sourceId)}`,
      payload
    );
    return response.data;
  },

  /**
   * Deletes an approved source.
   * @param {string} brandId
   * @param {string} sourceId
   */
  async deleteSource(brandId, sourceId) {
    const response = await apiClient.delete(
      `/brands/${encodeURIComponent(brandId)}/sources/${encodeURIComponent(sourceId)}`
    );
    return response.data;
  },

  /**
   * Retrieves team members for a brand workspace.
   * @param {string} brandId
   */
  async getMembers(brandId) {
    const response = await apiClient.get(`/brands/${encodeURIComponent(brandId)}/members`);
    return response.data || [];
  },

  /**
   * Invites or assigns a team member to a brand workspace.
   * @param {string} brandId
   * @param {{ fullName: string, email: string, role: string }} payload
   */
  async inviteMember(brandId, payload) {
    const response = await apiClient.post(
      `/brands/${encodeURIComponent(brandId)}/members`,
      payload
    );
    return response.data;
  },
};

export default brandService;
