import { apiClient } from './apiClient';

export const approvalService = {
  async getComments(reviewId) {
    const response = await apiClient.get(`/reviews/${encodeURIComponent(reviewId)}/comments`);
    return response.data || [];
  },

  async addComment(reviewId, commentText) {
    const response = await apiClient.post(`/reviews/${encodeURIComponent(reviewId)}/comments`, {
      commentText,
    });
    return response.data;
  },

  async getDecisions(reviewId) {
    const response = await apiClient.get(`/reviews/${encodeURIComponent(reviewId)}/decisions`);
    return response.data || [];
  },

  async makeDecision(reviewId, action, decisionNotes) {
    const response = await apiClient.post(`/reviews/${encodeURIComponent(reviewId)}/decisions`, {
      action,
      decisionNotes,
    });
    return response.data;
  },
};

export default approvalService;
