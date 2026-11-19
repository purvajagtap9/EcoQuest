import apiClient from './client'

export const adminApi = {
  getDashboard: () => apiClient.get('/api/admin/dashboard'),

  // Schools
  listSchools: (params) => apiClient.get('/api/admin/schools', { params }),
  getSchool: (id) => apiClient.get(`/api/admin/schools/${id}`),
  createSchool: (payload) => apiClient.post('/api/admin/schools', payload),
  updateSchool: (id, payload) => apiClient.put(`/api/admin/schools/${id}`, payload),
  deleteSchool: (id) => apiClient.delete(`/api/admin/schools/${id}`),

  // Students / teachers
  listStudents: (params) => apiClient.get('/api/admin/students', { params }),
  getStudent: (id) => apiClient.get(`/api/admin/students/${id}`),
  setStudentStatus: (id, enabled) => apiClient.put(`/api/admin/students/${id}/status`, { enabled }),
  deleteStudent: (id) => apiClient.delete(`/api/admin/students/${id}`),
  setLeaderboardPrivacy: (id, payload) => apiClient.put(`/api/admin/students/${id}/leaderboard-privacy`, payload),

  listTeachers: (params) => apiClient.get('/api/admin/teachers', { params }),
  getTeacher: (id) => apiClient.get(`/api/admin/teachers/${id}`),
  setTeacherStatus: (id, enabled) => apiClient.put(`/api/admin/teachers/${id}/status`, { enabled }),
  deleteTeacher: (id) => apiClient.delete(`/api/admin/teachers/${id}`),

  // Eco-coins
  listCoinTransactions: (studentId, params) => apiClient.get(`/api/admin/students/${studentId}/coin-transactions`, { params }),
  adjustCoins: (studentId, payload) => apiClient.post(`/api/admin/students/${studentId}/coins/adjust`, payload),

  // Learning content
  listTopics: () => apiClient.get('/api/admin/topics'),
  createTopic: (payload) => apiClient.post('/api/admin/topics', payload),
  updateTopic: (id, payload) => apiClient.put(`/api/admin/topics/${id}`, payload),
  deleteTopic: (id) => apiClient.delete(`/api/admin/topics/${id}`),

  listLessons: (topicId) => apiClient.get('/api/admin/lessons', { params: { topicId } }),
  createLesson: (payload) => apiClient.post('/api/admin/lessons', payload),
  updateLesson: (id, payload) => apiClient.put(`/api/admin/lessons/${id}`, payload),
  deleteLesson: (id) => apiClient.delete(`/api/admin/lessons/${id}`),

  listQuizzes: (topicId) => apiClient.get('/api/admin/quizzes', { params: { topicId } }),
  createQuiz: (payload) => apiClient.post('/api/admin/quizzes', payload),
  updateQuiz: (id, payload) => apiClient.put(`/api/admin/quizzes/${id}`, payload),
  deleteQuiz: (id) => apiClient.delete(`/api/admin/quizzes/${id}`),

  // Quests
  listQuests: (params) => apiClient.get('/api/admin/quests', { params }),
  createQuest: (payload) => apiClient.post('/api/admin/quests', payload),
  updateQuest: (id, payload) => apiClient.put(`/api/admin/quests/${id}`, payload),
  deleteQuest: (id) => apiClient.delete(`/api/admin/quests/${id}`),

  // Badges
  listBadges: () => apiClient.get('/api/admin/badges'),
  createBadge: (payload) => apiClient.post('/api/admin/badges', payload),
  updateBadge: (id, payload) => apiClient.put(`/api/admin/badges/${id}`, payload),
  deleteBadge: (id) => apiClient.delete(`/api/admin/badges/${id}`),

  // Submissions (override)
  listSubmissions: (params) => apiClient.get('/api/admin/submissions', { params }),
  getSubmission: (id) => apiClient.get(`/api/admin/submissions/${id}`),
  approveSubmission: (id) => apiClient.put(`/api/admin/submissions/${id}/approve`),
  rejectSubmission: (id, rejectionReason) => apiClient.put(`/api/admin/submissions/${id}/reject`, { rejectionReason }),

  // Leaderboard / Impact
  getLeaderboard: (scope, period, params) => apiClient.get('/api/admin/leaderboard', { params: { scope, period, ...params } }),
  getGlobalImpact: () => apiClient.get('/api/admin/analytics/impact'),
  listImpactFormulas: () => apiClient.get('/api/admin/impact-formulas'),
  upsertImpactFormula: (payload) => apiClient.put('/api/admin/impact-formulas', payload),
  resetImpactFormula: (category) => apiClient.delete(`/api/admin/impact-formulas/${category}`),
}
