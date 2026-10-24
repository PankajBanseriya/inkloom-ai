import { apiClient } from './apiClient';

export const reviewService = {
  /**
   * Submits content for brand governance review (deterministic + Gemini AI evaluation).
   * @param {{ brandId: string, title: string, contentType: string, content: string }} payload
   */
  async submitReview(payload) {
    const response = await apiClient.post('/reviews', payload);
    return response.data;
  },

  /**
   * Retrieves all reviews for a brand workspace.
   * @param {string} brandId
   */
  async getReviews(brandId) {
    const endpoint = brandId ? `/reviews?brandId=${encodeURIComponent(brandId)}` : '/reviews';
    const response = await apiClient.get(endpoint);
    return response.data || [];
  },

  /**
   * Retrieves review details by review ID.
   * @param {string} reviewId
   */
  async getReviewById(reviewId) {
    const response = await apiClient.get(`/reviews/${encodeURIComponent(reviewId)}`);
    return response.data;
  },

  /**
   * Retrieves findings for a review.
   * @param {string} reviewId
   */
  async getFindings(reviewId) {
    const response = await apiClient.get(`/reviews/${encodeURIComponent(reviewId)}/findings`);
    return response.data || [];
  },

  /**
   * Retrieves suggested corrections for a review.
   * @param {string} reviewId
   */
  async getSuggestions(reviewId) {
    const response = await apiClient.get(`/reviews/${encodeURIComponent(reviewId)}/suggestions`);
    return response.data || [];
  },

  /**
   * Accepts an Intent Lock correction and applies surgical non-destructive replacement.
   * @param {string} reviewId
   * @param {{ findingId: string, replacementText: string, tier: string, rationale: string }} payload
   */
  async acceptCorrection(reviewId, payload) {
    const response = await apiClient.post(`/reviews/${encodeURIComponent(reviewId)}/accept-correction`, payload);
    return response.data;
  },

  /**
   * Rejects an Intent Lock correction; records friction feedback and preserves author phrasing.
   * @param {string} reviewId
   * @param {string} findingId
   * @param {string} reason
   */
  async rejectCorrection(reviewId, findingId, reason = '') {
    const response = await apiClient.post(
      `/reviews/${encodeURIComponent(reviewId)}/reject-correction?findingId=${encodeURIComponent(findingId)}&reason=${encodeURIComponent(reason)}`
    );
    return response.data;
  },

  /**
   * Manually creates a new revision version.
   * @param {string} reviewId
   * @param {{ updatedContent: string, changeSummary: string }} payload
   */
  async saveRevision(reviewId, payload) {
    const response = await apiClient.post(`/reviews/${encodeURIComponent(reviewId)}/revisions`, payload);
    return response.data;
  },

  /**
   * Retrieves full revision history for a review.
   * @param {string} reviewId
   */
  async getRevisionHistory(reviewId) {
    const response = await apiClient.get(`/reviews/${encodeURIComponent(reviewId)}/history`);
    return response.data || [];
  },
};

export default reviewService;
