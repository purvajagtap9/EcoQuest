import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Coins, Zap, MapPin } from 'lucide-react'
import { studentApi } from '../../api/student'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

const DIFFICULTY_COLOR = {
  EASY: 'bg-eco-100 text-eco-700',
  MEDIUM: 'bg-yellow-100 text-yellow-700',
  HARD: 'bg-red-100 text-red-700',
}

export default function Quests() {
  const [quests, setQuests] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    studentApi
      .listQuests()
      .then((res) => setQuests(res.data))
      .catch(() => setError('Could not load quests.'))
  }, [])

  if (error) return <div className="max-w-5xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!quests) return <div className="max-w-5xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">🎯 Eco Quests</h1>

      {quests.length === 0 && <p className="text-gray-500">No quests available right now — check back soon.</p>}

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {quests.map((q) => (
          <Link key={q.id} to={`/student/quests/${q.id}`}>
            <Card className="h-full hover:shadow-md transition-shadow cursor-pointer">
              <div className="flex items-center justify-between mb-2">
                <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${DIFFICULTY_COLOR[q.difficulty]}`}>
                  {q.difficulty}
                </span>
                {q.gpsRequired && <MapPin size={14} className="text-gray-400" />}
              </div>
              <h3 className="font-semibold text-gray-800 mb-1">{q.title}</h3>
              <p className="text-sm text-gray-500 line-clamp-2 mb-3">{q.description}</p>
              <div className="flex items-center gap-4 text-sm">
                <span className="flex items-center gap-1 text-yellow-600 font-medium">
                  <Coins size={14} /> {q.ecoCoinsReward}
                </span>
                <span className="flex items-center gap-1 text-eco-600 font-medium">
                  <Zap size={14} /> {q.xpReward} XP
                </span>
              </div>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  )
}
