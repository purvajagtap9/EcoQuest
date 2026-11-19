import { useEffect, useState } from 'react'
import { MapPin } from 'lucide-react'
import { studentApi } from '../../api/student'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

const STATUS_STYLE = {
  PENDING: 'bg-gray-100 text-gray-600',
  AI_REVIEWED: 'bg-blue-100 text-blue-700',
  APPROVED: 'bg-eco-100 text-eco-700',
  REJECTED: 'bg-red-100 text-red-700',
}

export default function Submissions() {
  const [page, setPage] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    studentApi
      .listSubmissions({ page: 0, size: 20, sort: 'createdAt,desc' })
      .then((res) => setPage(res.data))
      .catch(() => setError('Could not load your submissions.'))
  }, [])

  if (error) return <div className="max-w-3xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!page) return <div className="max-w-3xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">My Submissions</h1>

      {page.content.length === 0 && <p className="text-gray-500">You haven't submitted any quest proof yet.</p>}

      <div className="space-y-3">
        {page.content.map((s) => (
          <Card key={s.id} className="flex gap-4">
            {s.imageUrl && (
              <img
                src={`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}${s.imageUrl}`}
                alt="Submission"
                className="w-20 h-20 object-cover rounded-md flex-shrink-0"
              />
            )}
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between mb-1">
                <h3 className="font-medium text-gray-800">{s.questTitle || 'Quest'}</h3>
                <span className={`text-xs font-medium px-2 py-0.5 rounded-full flex-shrink-0 ${STATUS_STYLE[s.status]}`}>
                  {s.status.replace('_', ' ')}
                </span>
              </div>
              {s.description && <p className="text-sm text-gray-500 line-clamp-2">{s.description}</p>}
              {s.status === 'REJECTED' && s.rejectionReason && (
                <p className="text-xs text-red-500 mt-1">Reason: {s.rejectionReason}</p>
              )}
              {s.withinAllowedRadius === false && (
                <p className="text-xs text-orange-500 flex items-center gap-1 mt-1">
                  <MapPin size={12} /> Submitted outside the expected location radius
                </p>
              )}
              <p className="text-xs text-gray-400 mt-1">{new Date(s.createdAt).toLocaleString()}</p>
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}
