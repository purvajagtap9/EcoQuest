import apiClient from './client'

export const teacherApi = {
  getDashboard: () => apiClient.get('/api/teacher/dashboard'),
  listStudents: (params) => apiClient.get('/api/teacher/students', { params }),

  listSubmissions: (params) => apiClient.get('/api/teacher/submissions', { params }),
  getSubmission: (id) => apiClient.get(`/api/teacher/submissions/${id}`),
  approveSubmission: (id) => apiClient.put(`/api/teacher/submissions/${id}/approve`),
  rejectSubmission: (id, rejectionReason) =>
    apiClient.put(`/api/teacher/submissions/${id}/reject`, { rejectionReason }),

  listQuests: (params) => apiClient.get('/api/teacher/quests', { params }),
  createQuest: (payload) => apiClient.post('/api/teacher/quests', payload),
  updateQuest: (id, payload) => apiClient.put(`/api/teacher/quests/${id}`, payload),
  deleteQuest: (id) => apiClient.delete(`/api/teacher/quests/${id}`),

  getLeaderboard: (period, params) => apiClient.get('/api/teacher/leaderboard', { params: { period, ...params } }),
  getImpact: () => apiClient.get('/api/teacher/impact'),
}
