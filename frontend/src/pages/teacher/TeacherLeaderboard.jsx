import { useEffect, useState } from 'react'
import { Trophy } from 'lucide-react'
import { teacherApi } from '../../api/teacher'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

const PERIODS = ['WEEKLY', 'MONTHLY', 'ALL_TIME']

export default function TeacherLeaderboard() {
  const [period, setPeriod] = useState('ALL_TIME')
  const [page, setPage] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    setPage(null)
    teacherApi
      .getLeaderboard(period, { page: 0, size: 30 })
      .then((res) => setPage(res.data))
      .catch(() => setError('Could not load the class leaderboard.'))
  }, [period])

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-2">
        <Trophy className="text-yellow-500" /> Class Leaderboard
      </h1>

      <div className="flex bg-white border border-gray-200 rounded-md overflow-hidden w-fit mb-5">
        {PERIODS.map((p) => (
          <button
            key={p}
            onClick={() => setPeriod(p)}
            className={`px-3 py-1.5 text-xs font-medium ${period === p ? 'bg-eco-600 text-white' : 'text-gray-600'}`}
          >
            {p.replace('_', ' ')}
          </button>
        ))}
      </div>

      {error && <p className="text-red-600">{error}</p>}
      {!page && !error && <div className="flex justify-center py-16"><Spinner size={32} /></div>}

      {page && (
        <Card className="!p-0 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-eco-50 text-gray-500 text-xs uppercase">
              <tr>
                <th className="text-left px-4 py-3">Rank</th>
                <th className="text-left px-4 py-3">Student</th>
                <th className="text-right px-4 py-3">Eco-Coins</th>
                <th className="text-right px-4 py-3 hidden sm:table-cell">Badges</th>
              </tr>
            </thead>
            <tbody>
              {page.content.map((row) => (
                <tr key={row.studentId} className="border-t border-gray-50">
                  <td className="px-4 py-3 text-gray-500">#{row.rank}</td>
                  <td className="px-4 py-3 text-gray-800">{row.displayName}</td>
                  <td className="px-4 py-3 text-right font-semibold text-yellow-600">{row.ecoCoins.toLocaleString()}</td>
                  <td className="px-4 py-3 text-right hidden sm:table-cell">{row.badgeCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {page.content.length === 0 && <p className="text-gray-400 text-sm text-center py-8">No students yet.</p>}
        </Card>
      )}
    </div>
  )
}
