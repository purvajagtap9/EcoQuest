import { useEffect, useState } from 'react'
import { studentApi } from '../../api/student'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

export default function Badges() {
  const [badges, setBadges] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    studentApi
      .listBadges()
      .then((res) => setBadges(res.data))
      .catch(() => setError('Could not load badges.'))
  }, [])

  if (error) return <div className="max-w-4xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!badges) return <div className="max-w-4xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">🏆 Badges</h1>

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
        {badges.map((b) => (
          <Card
            key={b.badgeId}
            className={`text-center ${b.earned ? '' : 'opacity-40 grayscale'}`}
          >
            <div className="text-4xl mb-2">{b.icon || '🏅'}</div>
            <h3 className="font-semibold text-sm text-gray-800">{b.name}</h3>
            <p className="text-xs text-gray-500 mt-1">{b.description}</p>
            {b.earned && b.earnedAt && (
              <p className="text-[11px] text-eco-600 mt-2">Earned {new Date(b.earnedAt).toLocaleDateString()}</p>
            )}
          </Card>
        ))}
      </div>
    </div>
  )
}
