import { useEffect, useState } from 'react'
import { Clock, CheckCircle2, XCircle, Users } from 'lucide-react'
import { teacherApi } from '../../api/teacher'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useAuth } from '../../context/AuthContext'

export default function TeacherDashboard() {
  const { user } = useAuth()
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    teacherApi
      .getDashboard()
      .then((res) => setData(res.data))
      .catch(() => setError('Could not load your dashboard.'))
  }, [])

  if (error) return <div className="max-w-6xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!data) return <div className="max-w-6xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-1">Welcome, {user?.name} 👨‍🏫</h1>
      <p className="text-gray-500 mb-6">Here's what needs your attention today.</p>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={<Clock size={20} />} label="Pending Reviews" value={data.pendingReviews} accent="text-orange-600" />
        <StatCard icon={<CheckCircle2 size={20} />} label="Approved Today" value={data.approvedToday} accent="text-eco-600" />
        <StatCard icon={<XCircle size={20} />} label="Rejected Today" value={data.rejectedToday} accent="text-red-600" />
        <StatCard icon={<Users size={20} />} label="Students" value={data.totalStudents} accent="text-blue-600" />
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
