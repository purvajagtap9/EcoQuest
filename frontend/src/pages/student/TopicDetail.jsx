import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ArrowLeft, MapPin, HelpCircle } from 'lucide-react'
import { learningApi } from '../../api/learning'
import apiClient from '../../api/client'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

export default function TopicDetail() {
  const { topicId } = useParams()
  const [topic, setTopic] = useState(null)
  const [quizzes, setQuizzes] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    learningApi
      .getTopic(topicId)
      .then((res) => setTopic(res.data))
      .catch(() => setError('Could not load this topic.'))

    apiClient
      .get('/api/quizzes', { params: { topicId } })
      .then((res) => setQuizzes(res.data))
      .catch(() => {})
  }, [topicId])

  if (error) return <div className="max-w-3xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!topic) return <div className="max-w-3xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <Link to="/student/learn" className="inline-flex items-center gap-1 text-sm text-eco-700 mb-4">
        <ArrowLeft size={16} /> Back to Learn
      </Link>

      <h1 className="text-2xl font-bold text-gray-800 mb-2">{topic.title}</h1>
      <p className="text-gray-500 mb-6">{topic.description}</p>

      {topic.learningObjectives?.length > 0 && (
        <Card className="mb-6">
          <h2 className="font-semibold text-gray-700 mb-2">What you'll learn</h2>
          <ul className="list-disc list-inside text-sm text-gray-600 space-y-1">
            {topic.learningObjectives.map((o, i) => <li key={i}>{o}</li>)}
          </ul>
        </Card>
      )}

      <div className="space-y-4 mb-6">
        {topic.lessons?.map((lesson) => (
          <Card key={lesson.id}>
            <h3 className="font-semibold text-gray-800 mb-2">{lesson.title}</h3>
            <p className="text-sm text-gray-600 leading-relaxed mb-3">{lesson.body}</p>
            {lesson.localContextNote && (
              <div className="flex gap-2 bg-eco-50 border border-eco-100 rounded-md p-3 text-xs text-eco-800">
                <MapPin size={14} className="flex-shrink-0 mt-0.5" />
                <span>{lesson.localContextNote}</span>
              </div>
            )}
          </Card>
        ))}
        {(!topic.lessons || topic.lessons.length === 0) && (
          <p className="text-gray-500 text-sm">No lessons published for this topic yet.</p>
        )}
      </div>

      {quizzes.length > 0 && (
        <Card>
          <h2 className="font-semibold text-gray-700 mb-3">Test your knowledge</h2>
          <div className="space-y-2">
            {quizzes.map((quiz) => (
              <Link
                key={quiz.id}
                to={`/student/quiz/${quiz.id}`}
                className="flex items-center justify-between border border-gray-100 rounded-md px-4 py-3 hover:border-eco-300 hover:bg-eco-50 transition-colors"
              >
                <span className="flex items-center gap-2 text-sm font-medium text-gray-700">
                  <HelpCircle size={16} className="text-eco-600" /> {quiz.title}
                </span>
                <span className="text-xs text-gray-400">{quiz.questionCount} questions</span>
              </Link>
            ))}
          </div>
        </Card>
      )}
    </div>
  )
}
