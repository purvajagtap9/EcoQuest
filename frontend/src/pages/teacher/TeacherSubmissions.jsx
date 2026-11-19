import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { teacherApi } from '../../api/teacher'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

const STATUS_STYLE = {
  PENDING: 'bg-gray-100 text-gray-600',
  AI_REVIEWED: 'bg-blue-100 text-blue-700',
  APPROVED: 'bg-eco-100 text-eco-700',
  REJECTED: 'bg-red-100 text-red-700',
}

const FILTERS = [
  { label: 'Pending', value: 'PENDING' },
  { label: 'AI Reviewed', value: 'AI_REVIEWED' },
  { label: 'Approved', value: 'APPROVED' },
  { label: 'Rejected', value: 'REJECTED' },
  { label: 'All', value: '' },
]

export default function TeacherSubmissions() {
  const [status, setStatus] = useState('PENDING')
  const [page, setPage] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    setPage(null)
    const params = { page: 0, size: 30, sort: 'createdAt,desc' }
    if (status) params.status = status
    teacherApi
      .listSubmissions(params)
      .then((res) => setPage(res.data))
      .catch(() => setError('Could not load submissions.'))
  }, [status])

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">📸 Submissions to Review</h1>

      <div className="flex flex-wrap gap-2 mb-5">
        {FILTERS.map((f) => (
          <button
            key={f.value}
            onClick={() => setStatus(f.value)}
            className={`px-3 py-1.5 text-sm font-medium rounded-md border ${
              status === f.value ? 'bg-eco-600 text-white border-eco-600' : 'border-gray-200 text-gray-600'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {error && <p className="text-red-600">{error}</p>}
      {!page && !error && <div className="flex justify-center py-16"><Spinner size={32} /></div>}

      {page && page.content.length === 0 && <p className="text-gray-500">Nothing here.</p>}

      <div className="space-y-3">
        {page?.content.map((s) => (
          <Link key={s.id} to={`/teacher/submissions/${s.id}`}>
            <Card className="flex items-center gap-4 hover:shadow-md transition-shadow cursor-pointer">
              {s.imageUrl && (
                <img
                  src={`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}${s.imageUrl}`}
                  alt="Submission"
                  className="w-16 h-16 object-cover rounded-md flex-shrink-0"
                />
              )}
              <div className="flex-1 min-w-0">
                <div className="flex items-center justify-between">
                  <h3 className="font-medium text-gray-800 truncate">{s.questTitle}</h3>
                  <span className={`text-xs font-medium px-2 py-0.5 rounded-full flex-shrink-0 ml-2 ${STATUS_STYLE[s.status]}`}>
                    {s.status.replace('_', ' ')}
                  </span>
                </div>
                <p className="text-sm text-gray-500">{s.studentName}</p>
                <p className="text-xs text-gray-400">{new Date(s.createdAt).toLocaleString()}</p>
              </div>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  )
}
