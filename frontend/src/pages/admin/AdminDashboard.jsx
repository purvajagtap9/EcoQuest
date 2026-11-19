import { useEffect, useState } from 'react'
import { School, GraduationCap, Users, Target, Coins, Award, TreePine } from 'lucide-react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts'
import { adminApi } from '../../api/admin'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

export default function AdminDashboard() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    adminApi
      .getDashboard()
      .then((res) => setData(res.data))
      .catch(() => setError('Could not load the dashboard.'))
  }, [])

  if (error) return <div className="max-w-6xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!data) return <div className="max-w-6xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  const chartData = [
    { name: 'Completed', value: data.completedQuests },
    { name: 'Pending', value: data.pendingVerifications },
  ]

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Platform Overview</h1>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard icon={<GraduationCap size={20} />} label="Students" value={data.totalStudents} accent="text-eco-600" />
        <StatCard icon={<Users size={20} />} label="Teachers" value={data.totalTeachers} accent="text-blue-600" />
        <StatCard icon={<School size={20} />} label="Schools" value={data.totalSchools} accent="text-purple-600" />
        <StatCard icon={<Target size={20} />} label="Total Quests" value={data.totalQuests} accent="text-orange-600" />
        <StatCard icon={<Coins size={20} />} label="Eco-Coins Issued" value={data.totalEcoCoins.toLocaleString()} accent="text-yellow-600" />
        <StatCard icon={<Award size={20} />} label="Badges Unlocked" value={data.totalBadgesUnlocked} accent="text-pink-600" />
        <StatCard icon={<TreePine size={20} />} label="Trees Planted (est.)" value={data.totalTreesPlanted} accent="text-eco-700" />
        <StatCard label="Active Users" value={data.activeUsers} accent="text-gray-600" />
      </div>

      <Card>
        <h2 className="font-semibold text-gray-700 mb-4">Quest Completion Status</h2>
        <ResponsiveContainer width="100%" height={240}>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0fdf4" />
            <XAxis dataKey="name" tick={{ fontSize: 12 }} />
            <YAxis tick={{ fontSize: 12 }} />
            <Tooltip />
            <Bar dataKey="value" fill="#22c55e" radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
        <p className="text-xs text-gray-400 mt-2">
          Approval rate: {(data.approvalRate * 100).toFixed(1)}% · Completion rate: {(data.questCompletionRate * 100).toFixed(1)}%
        </p>
      </Card>
    </div>
  )
}

function StatCard({ icon, label, value, accent }) {
  return (
    <Card className="flex items-center gap-3">
      {icon && <div className={`${accent} bg-gray-50 rounded-lg p-2`}>{icon}</div>}
      <div>
        <p className="text-xl font-bold text-gray-800 leading-none">{value}</p>
        <p className="text-xs text-gray-400 mt-1">{label}</p>
      </div>
    </Card>
  )
}
