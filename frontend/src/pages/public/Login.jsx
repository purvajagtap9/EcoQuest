import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Leaf } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'

export default function Login() {
  const { login, loading } = useAuth()
  const { showToast } = useToast()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    try {
      const user = await login(email, password)
      navigate(`/${user.role.toLowerCase()}/dashboard`)
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Check your credentials.')
      showToast('Login failed', 'error')
    }
  }

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4">
      <div className="w-full max-w-sm bg-white rounded-xl shadow-sm border border-eco-100 p-8">
        <div className="flex items-center gap-2 text-eco-700 font-bold text-xl mb-6 justify-center">
          <Leaf size={28} /> EcoQuest India
        </div>
        <h1 className="text-lg font-semibold text-center mb-6">Welcome back</h1>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-eco-400"
              placeholder="student@ecoquest.com"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-600 mb-1">Password</label>
            <input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-eco-400"
              placeholder="••••••••"
            />
          </div>

          {error && <p className="text-sm text-red-600">{error}</p>}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-eco-600 text-white rounded-md py-2 text-sm font-medium hover:bg-eco-700 disabled:opacity-60"
          >
            {loading ? 'Logging in...' : 'Log In'}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-6">
          Don't have an account?{' '}
          <Link to="/register" className="text-eco-700 font-medium">Register</Link>
        </p>

        <div className="mt-6 pt-4 border-t border-gray-100 text-xs text-gray-400 text-center">
          Demo accounts: student@ecoquest.com / teacher@ecoquest.com / admin@ecoquest.com
        </div>
      </div>
    </div>
  )
}
