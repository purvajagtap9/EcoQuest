import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ToastProvider } from './context/ToastContext'
import ProtectedRoute from './components/ProtectedRoute'
import AppLayout from './components/layout/AppLayout'

import Home from './pages/public/Home'
import Login from './pages/public/Login'
import Register from './pages/public/Register'
import About from './pages/public/About'
import HowItWorks from './pages/public/HowItWorks'

import StudentDashboard from './pages/student/StudentDashboard'
import Learn from './pages/student/Learn'
import TopicDetail from './pages/student/TopicDetail'
import QuizPage from './pages/student/QuizPage'
import Quests from './pages/student/Quests'
import QuestDetail from './pages/student/QuestDetail'
import Submissions from './pages/student/Submissions'
import Badges from './pages/student/Badges'
import Leaderboard from './pages/student/Leaderboard'
import Impact from './pages/student/Impact'
import TeacherDashboard from './pages/teacher/TeacherDashboard'
import TeacherSubmissions from './pages/teacher/TeacherSubmissions'
import TeacherSubmissionDetail from './pages/teacher/TeacherSubmissionDetail'
import TeacherQuests from './pages/teacher/TeacherQuests'
import TeacherStudents from './pages/teacher/TeacherStudents'
import TeacherLeaderboard from './pages/teacher/TeacherLeaderboard'
import TeacherImpact from './pages/teacher/TeacherImpact'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminStudents from './pages/admin/AdminStudents'
import AdminTeachers from './pages/admin/AdminTeachers'
import AdminSchools from './pages/admin/AdminSchools'
import AdminTopics from './pages/admin/AdminTopics'
import AdminQuizzes from './pages/admin/AdminQuizzes'
import AdminQuests from './pages/admin/AdminQuests'
import AdminBadges from './pages/admin/AdminBadges'
import AdminSubmissions from './pages/admin/AdminSubmissions'
import AdminSubmissionDetail from './pages/admin/AdminSubmissionDetail'
import AdminAnalytics from './pages/admin/AdminAnalytics'

function NotFound() {
  return (
    <div className="max-w-xl mx-auto px-4 py-24 text-center">
      <h1 className="text-2xl font-bold text-gray-800 mb-2">Page not found</h1>
      <p className="text-gray-500">
        This page hasn't been built yet in this in-progress prototype, or the URL is wrong.
      </p>
    </div>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            <Route element={<AppLayout />}>
              {/* Public */}
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/about" element={<About />} />
              <Route path="/how-it-works" element={<HowItWorks />} />

              {/* Student */}
              <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
                <Route path="/student/dashboard" element={<StudentDashboard />} />
                <Route path="/student/learn" element={<Learn />} />
                <Route path="/student/learn/:topicId" element={<TopicDetail />} />
                <Route path="/student/quiz/:quizId" element={<QuizPage />} />
                <Route path="/student/quests" element={<Quests />} />
                <Route path="/student/quests/:questId" element={<QuestDetail />} />
                <Route path="/student/submissions" element={<Submissions />} />
                <Route path="/student/badges" element={<Badges />} />
                <Route path="/student/leaderboard" element={<Leaderboard />} />
                <Route path="/student/impact" element={<Impact />} />
              </Route>

              {/* Teacher */}
              <Route element={<ProtectedRoute allowedRoles={['TEACHER']} />}>
                <Route path="/teacher/dashboard" element={<TeacherDashboard />} />
                <Route path="/teacher/submissions" element={<TeacherSubmissions />} />
                <Route path="/teacher/submissions/:submissionId" element={<TeacherSubmissionDetail />} />
                <Route path="/teacher/quests" element={<TeacherQuests />} />
                <Route path="/teacher/students" element={<TeacherStudents />} />
                <Route path="/teacher/leaderboard" element={<TeacherLeaderboard />} />
                <Route path="/teacher/impact" element={<TeacherImpact />} />
              </Route>

              {/* Admin */}
              <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                <Route path="/admin/dashboard" element={<AdminDashboard />} />
                <Route path="/admin/students" element={<AdminStudents />} />
                <Route path="/admin/teachers" element={<AdminTeachers />} />
                <Route path="/admin/schools" element={<AdminSchools />} />
                <Route path="/admin/topics" element={<AdminTopics />} />
                <Route path="/admin/quizzes" element={<AdminQuizzes />} />
                <Route path="/admin/quests" element={<AdminQuests />} />
                <Route path="/admin/badges" element={<AdminBadges />} />
                <Route path="/admin/submissions" element={<AdminSubmissions />} />
                <Route path="/admin/submissions/:submissionId" element={<AdminSubmissionDetail />} />
                <Route path="/admin/analytics" element={<AdminAnalytics />} />
              </Route>

              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
