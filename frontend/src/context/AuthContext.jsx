import { createContext, useContext, useEffect, useState } from 'react'
import apiClient from '../api/client'

const AuthContext = createContext(null)

const TOKEN_KEY = 'ecoquest_token'
const USER_KEY = 'ecoquest_user'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  })
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (user) {
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    } else {
      localStorage.removeItem(USER_KEY)
    }
  }, [user])

  async function login(email, password) {
    setLoading(true)
    try {
      const res = await apiClient.post('/api/auth/login', { email, password })
      localStorage.setItem(TOKEN_KEY, res.data.token)
      const loggedInUser = {
        userId: res.data.userId,
        name: res.data.name,
        email: res.data.email,
        role: res.data.role,
      }
      setUser(loggedInUser)
      return loggedInUser
    } finally {
      setLoading(false)
    }
  }

  async function register(payload) {
    setLoading(true)
    try {
      const res = await apiClient.post('/api/auth/register', payload)
      localStorage.setItem(TOKEN_KEY, res.data.token)
      const registeredUser = {
        userId: res.data.userId,
        name: res.data.name,
        email: res.data.email,
        role: res.data.role,
      }
      setUser(registeredUser)
      return registeredUser
    } finally {
      setLoading(false)
    }
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
