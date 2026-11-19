import { Link, Outlet, useNavigate } from 'react-router-dom'
import { Leaf, LogOut, Menu, X } from 'lucide-react'
import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'

const NAV_LINKS = {
  STUDENT: [
    { to: '/student/dashboard', label: 'Dashboard' },
    { to: '/student/learn', label: 'Learn' },
    { to: '/student/quests', label: 'Quests' },
    { to: '/student/submissions', label: 'Submissions' },
    { to: '/student/badges', label: 'Badges' },
    { to: '/student/leaderboard', label: 'Leaderboard' },
    { to: '/student/impact', label: 'Impact' },
  ],
  TEACHER: [
    { to: '/teacher/dashboard', label: 'Dashboard' },
    { to: '/teacher/quests', label: 'Quests' },
    { to: '/teacher/submissions', label: 'Submissions' },
    { to: '/teacher/students', label: 'Students' },
    { to: '/teacher/leaderboard', label: 'Leaderboard' },
    { to: '/teacher/impact', label: 'Impact' },
  ],
  ADMIN: [
    { to: '/admin/dashboard', label: 'Dashboard' },
    { to: '/admin/students', label: 'Students' },
    { to: '/admin/teachers', label: 'Teachers' },
    { to: '/admin/schools', label: 'Schools' },
    { to: '/admin/topics', label: 'Topics' },
    { to: '/admin/quests', label: 'Quests' },
    { to: '/admin/badges', label: 'Badges' },
    { to: '/admin/submissions', label: 'Submissions' },
    { to: '/admin/analytics', label: 'Analytics' },
  ],
}

export default function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [mobileOpen, setMobileOpen] = useState(false)

  const links = user ? NAV_LINKS[user.role] || [] : []
  const homePath = user ? `/${user.role.toLowerCase()}/dashboard` : '/'

  function handleLogout() {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white border-b border-eco-100 sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-4 flex items-center justify-between h-16">
          <Link to={homePath} className="flex items-center gap-2 text-eco-700 font-bold text-lg">
            <Leaf size={24} />
            EcoQuest India
          </Link>

          <nav className="hidden md:flex items-center gap-1">
            {links.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className="px-3 py-2 text-sm font-medium text-gray-600 hover:text-eco-700 hover:bg-eco-50 rounded-md transition-colors"
              >
                {link.label}
              </Link>
            ))}
          </nav>

          <div className="hidden md:flex items-center gap-3">
            {user ? (
              <>
                <span className="text-sm text-gray-500">{user.name}</span>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-1 text-sm font-medium text-gray-600 hover:text-red-600 px-3 py-2"
                >
                  <LogOut size={16} /> Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-sm font-medium text-gray-600 hover:text-eco-700 px-3 py-2">
                  Login
                </Link>
                <Link
                  to="/register"
                  className="text-sm font-medium bg-eco-600 text-white px-4 py-2 rounded-md hover:bg-eco-700"
                >
                  Get Started
                </Link>
              </>
            )}
          </div>

          <button className="md:hidden" onClick={() => setMobileOpen((v) => !v)}>
            {mobileOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>

        {mobileOpen && (
          <div className="md:hidden border-t border-eco-100 px-4 py-3 flex flex-col gap-1">
            {links.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                onClick={() => setMobileOpen(false)}
                className="px-3 py-2 text-sm font-medium text-gray-600 hover:text-eco-700 hover:bg-eco-50 rounded-md"
              >
                {link.label}
              </Link>
            ))}
            {user ? (
              <button onClick={handleLogout} className="text-left px-3 py-2 text-sm font-medium text-red-600">
                Logout
              </button>
            ) : (
              <>
                <Link to="/login" className="px-3 py-2 text-sm font-medium text-gray-600">Login</Link>
                <Link to="/register" className="px-3 py-2 text-sm font-medium text-eco-700">Get Started</Link>
              </>
            )}
          </div>
        )}
      </header>

      <main className="flex-1 bg-eco-50">
        <Outlet />
      </main>

      <footer className="bg-white border-t border-eco-100 py-6 text-center text-sm text-gray-400">
        EcoQuest India — SIH Prototype
      </footer>
    </div>
  )
}
