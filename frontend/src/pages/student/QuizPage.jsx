import { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { CheckCircle2, XCircle, Coins, Zap } from 'lucide-react'
import { learningApi } from '../../api/learning'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

export default function QuizPage() {
  const { quizId } = useParams()
  const [quiz, setQuiz] = useState(null)
  const [answers, setAnswers] = useState({})
  const [result, setResult] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    learningApi
      .getQuiz(quizId)
      .then((res) => setQuiz(res.data))
      .catch(() => setError('Could not load this quiz.'))
  }, [quizId])

  function selectAnswer(questionId, optionIndex) {
    setAnswers((prev) => ({ ...prev, [questionId]: optionIndex }))
  }

  async function handleSubmit() {
    setSubmitting(true)
    setError('')
    try {
      const res = await learningApi.submitQuizAttempt(quizId, answers)
      setResult(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Could not submit your answers.')
    } finally {
      setSubmitting(false)
    }
  }

  if (error && !quiz) return <div className="max-w-2xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!quiz) return <div className="max-w-2xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  if (result) {
    return (
      <div className="max-w-2xl mx-auto px-4 py-8">
        <Card className="text-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800 mb-1">🎉 Quiz Completed!</h1>
          <p className="text-gray-500 mb-4">Score: {result.score}/{result.totalQuestions}</p>
          {result.rewarded ? (
            <div className="flex items-center justify-center gap-6">
              <span className="flex items-center gap-1 text-yellow-600 font-semibold">
                <Coins size={18} /> +{result.coinsEarned} Eco-Coins
              </span>
              <span className="flex items-center gap-1 text-eco-600 font-semibold">
                <Zap size={18} /> +{result.xpEarned} XP
              </span>
            </div>
          ) : (
            <p className="text-sm text-gray-400">You've already earned rewards for this quiz — great practice run!</p>
          )}
        </Card>

        <div className="space-y-3">
          {result.results.map((r) => (
            <Card key={r.questionId} className={r.correct ? 'border-eco-200' : 'border-red-200'}>
              <div className="flex items-start gap-2 mb-1">
                {r.correct ? (
                  <CheckCircle2 size={18} className="text-eco-600 flex-shrink-0 mt-0.5" />
                ) : (
                  <XCircle size={18} className="text-red-500 flex-shrink-0 mt-0.5" />
                )}
                <p className="font-medium text-gray-800 text-sm">{r.text}</p>
              </div>
              <p className="text-xs text-gray-500 ml-6">{r.explanation}</p>
            </Card>
          ))}
        </div>

        <Link to="/student/learn" className="block text-center text-eco-700 font-medium mt-6">
          Back to Learn
        </Link>
      </div>
    )
  }

  const allAnswered = quiz.questions.every((q) => answers[q.id] !== undefined)

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">{quiz.title}</h1>

      <div className="space-y-5">
        {quiz.questions.map((q, i) => (
          <Card key={q.id}>
            <p className="font-medium text-gray-800 mb-3">{i + 1}. {q.text}</p>
            <div className="space-y-2">
              {q.options.map((opt, idx) => (
                <button
                  key={idx}
                  onClick={() => selectAnswer(q.id, idx)}
                  className={`w-full text-left px-4 py-2.5 rounded-md border text-sm transition-colors ${
                    answers[q.id] === idx
                      ? 'border-eco-500 bg-eco-50 text-eco-800 font-medium'
                      : 'border-gray-200 hover:border-eco-200'
                  }`}
                >
                  {opt}
                </button>
              ))}
            </div>
          </Card>
        ))}
      </div>

      {error && <p className="text-red-600 text-sm mt-4">{error}</p>}

      <button
        onClick={handleSubmit}
        disabled={!allAnswered || submitting}
        className="w-full mt-6 bg-eco-600 text-white rounded-md py-3 font-medium hover:bg-eco-700 disabled:opacity-50"
      >
        {submitting ? 'Submitting...' : 'Submit Quiz'}
      </button>
    </div>
  )
}
