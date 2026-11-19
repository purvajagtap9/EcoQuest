import { useEffect, useState } from 'react'
import { teacherApi } from '../../api/teacher'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

export default function TeacherStudents() {
  const [page, setPage] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    teacherApi
      .listStudents({ page: 0, size: 50, sort: 'name,asc' })
      .then((res) => setPage(res.data))
      .catch(() => setError('Could not load your students.'))
  }, [])

  if (error) return <div className="max-w-3xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!page) return <div className="max-w-3xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Students ({page.totalElements})</h1>

      <Card className="!p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-eco-50 text-gray-500 text-xs uppercase">
            <tr>
              <th className="text-left px-4 py-3">Name</th>
              <th className="text-left px-4 py-3 hidden sm:table-cell">Grade</th>
              <th className="text-right px-4 py-3">Eco-Coins</th>
              <th className="text-right px-4 py-3 hidden sm:table-cell">Streak</th>
            </tr>
          </thead>
          <tbody>
            {page.content.map((s) => (
              <tr key={s.userId} className="border-t border-gray-50">
                <td className="px-4 py-3 text-gray-800">{s.name}</td>
                <td className="px-4 py-3 text-gray-500 hidden sm:table-cell">{s.grade || '—'}</td>
                <td className="px-4 py-3 text-right font-medium text-yellow-600">{s.ecoCoins}</td>
                <td className="px-4 py-3 text-right text-gray-500 hidden sm:table-cell">🔥 {s.currentStreak}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {page.content.length === 0 && <p className="text-gray-400 text-sm text-center py-8">No students in your school yet.</p>}
      </Card>
    </div>
  )
}
