import apiClient from './client'

export const learningApi = {
  listTopics: (lang = 'en') => apiClient.get('/api/topics', { params: { lang } }),
  getTopic: (id, lang = 'en') => apiClient.get(`/api/topics/${id}`, { params: { lang } }),
  getQuiz: (id) => apiClient.get(`/api/quizzes/${id}`),
  submitQuizAttempt: (id, answers) => apiClient.post(`/api/quizzes/${id}/attempt`, { answers }),
}
