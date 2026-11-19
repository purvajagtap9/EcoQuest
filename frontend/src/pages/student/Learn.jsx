import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BookOpen } from 'lucide-react'
import { learningApi } from '../../api/learning'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

const LANGUAGES = [
  { code: 'en', label: 'English' },
  { code: 'hi', label: 'हिंदी' },
  { code: 'mr', label: 'मराठी' },
]

export default function Learn() {
  const [topics, setTopics] = useState(null)
  const [lang, setLang] = useState('en')
  const [error, setError] = useState('')

  useEffect(() => {
    setTopics(null)
    learningApi
      .listTopics(lang)
      .then((res) => setTopics(res.data))
      .catch(() => setError('Could not load topics.'))
  }, [lang])

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Learn EVS</h1>
        <select
          value={lang}
          onChange={(e) => setLang(e.target.value)}
          className="border border-gray-200 rounded-md px-3 py-1.5 text-sm"
        >
          {LANGUAGES.map((l) => (
            <option key={l.code} value={l.code}>{l.label}</option>
          ))}
        </select>
      </div>

      {error && <p className="text-red-600">{error}</p>}
      {!topics && !error && <div className="flex justify-center py-16"><Spinner size={32} /></div>}

      {topics && topics.length === 0 && (
        <p className="text-gray-500">No topics published yet — check back soon.</p>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {topics?.map((topic) => (
          <Link key={topic.id} to={`/student/learn/${topic.id}`}>
            <Card className="h-full hover:shadow-md transition-shadow cursor-pointer">
              <div className="text-eco-600 mb-3"><BookOpen size={22} /></div>
              <h3 className="font-semibold text-gray-800 mb-1">{topic.title}</h3>
              <p className="text-sm text-gray-500 line-clamp-2">{topic.description}</p>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  )
}
