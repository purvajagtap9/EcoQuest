import apiClient from './client'

export const studentApi = {
  getProfile: () => apiClient.get('/api/student/profile'),
  getDashboard: () => apiClient.get('/api/student/dashboard'),

  listQuests: () => apiClient.get('/api/student/quests'),
  getQuest: (id) => apiClient.get(`/api/student/quests/${id}`),
  submitQuestProof: (questId, formData) =>
    apiClient.post(`/api/quests/${questId}/submit`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),

  listSubmissions: (params) => apiClient.get('/api/student/submissions', { params }),
  getSubmission: (id) => apiClient.get(`/api/student/submissions/${id}`),

  listCoinTransactions: (params) => apiClient.get('/api/student/coin-transactions', { params }),
  listBadges: () => apiClient.get('/api/student/badges'),

  getLeaderboard: (scope, period, params) =>
    apiClient.get('/api/student/leaderboard', { params: { scope, period, ...params } }),

  getImpact: () => apiClient.get('/api/student/impact'),
}
