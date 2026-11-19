import { useEffect, useState } from 'react'
import { Coins, Flame, Target, Award, TrendingUp, Clock } from 'lucide-react'
import { studentApi } from '../../api/student'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useLeaderboardSocket } from '../../utils/useLeaderboardSocket'

export default function StudentDashboard() {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    studentApi
      .getDashboard()
      .then((res) => setData(res.data))
      .catch(() => setError('Could not load your dashboard.'))
  }, [])

  // Live rank-change push (Phase 18) - shows a toast the moment a teacher approves something
  useLeaderboardSocket((msg) => showToast(msg.message, 'success'))

  if (error) return <div className="max-w-6xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!data) return <div className="max-w-6xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  const xpProgress = data.xpForNextLevel > 0 ? Math.min(100, (data.xp / data.xpForNextLevel) * 100) : 100

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Welcome back, {data.name}! 🌱</h1>
      <p className="text-gray-500 mb-6">{data.levelTitle}</p>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard icon={<Coins size={20} />} label="Eco-Coins" value={data.ecoCoins.toLocaleString()} accent="text-yellow-600" />
        <StatCard icon={<Flame size={20} />} label="Day Streak" value={data.currentStreak} accent="text-orange-600" />
        <StatCard icon={<Target size={20} />} label="Completed Quests" value={data.completedQuests} accent="text-eco-600" />
        <StatCard icon={<Clock size={20} />} label="Pending Review" value={data.pendingVerification} accent="text-blue-600" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <Card className="lg:col-span-2">
          <div className="flex items-center justify-between mb-2">
            <h2 className="font-semibold text-gray-700">Level Progress</h2>
            <span className="text-sm text-gray-400">Level {data.level}</span>
          </div>
          <div className="w-full bg-gray-100 rounded-full h-3 mb-1">
            <div className="bg-eco-500 h-3 rounded-full transition-all" style={{ width: `${xpProgress}%` }} />
          </div>
          <p className="text-xs text-gray-400">
            {data.xp} XP {data.xpForNextLevel > 0 ? `/ ${data.xpForNextLevel} XP to next level` : '(max level)'}
          </p>
        </Card>

        <Card>
          <div className="flex items-center gap-2 mb-2">
            <TrendingUp size={18} className="text-eco-600" />
            <h2 className="font-semibold text-gray-700">Your Rank</h2>
          </div>
          <p className="text-3xl font-bold text-eco-700">#{data.rank}</p>
          <p className="text-xs text-gray-400 mt-1">Across all students, all-time</p>
        </Card>

        <Card>
          <div className="flex items-center gap-2 mb-2">
            <Award size={18} className="text-eco-600" />
            <h2 className="font-semibold text-gray-700">Badges Earned</h2>
          </div>
          <p className="text-3xl font-bold text-eco-700">{data.badgeCount}</p>
        </Card>
      </div>
    </div>
  )
}

function StatCard({ icon, label, value, accent }) {
  return (
    <Card className="flex items-center gap-3">
      <div className={`${accent} bg-gray-50 rounded-lg p-2`}>{icon}</div>
      <div>
        <p className="text-xl font-bold text-gray-800 leading-none">{value}</p>
        <p className="text-xs text-gray-400 mt-1">{label}</p>
      </div>
    </Card>
  )
}
