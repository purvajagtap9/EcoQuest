import { useEffect, useState } from 'react'
import { Plus, Trash2 } from 'lucide-react'
import { adminApi } from '../../api/admin'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

const EMPTY_QUESTION = () => ({ text: '', options: ['', '', '', ''], correctOptionIndex: 0, explanation: '' })
const EMPTY_FORM = { title: '', coinsPerCorrectAnswer: 5, xpPerCorrectAnswer: 10, questions: [EMPTY_QUESTION()] }

export default function AdminQuizzes() {
  const { showToast } = useToast()
  const [topics, setTopics] = useState([])
  const [topicId, setTopicId] = useState('')
  const [quizzes, setQuizzes] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    adminApi.listTopics().then((res) => {
      setTopics(res.data)
      if (res.data.length > 0) setTopicId(res.data[0].id)
    })
  }, [])

  useEffect(() => {
    if (!topicId) return
    adminApi.listQuizzes(topicId).then((res) => setQuizzes(res.data))
  }, [topicId])

  function updateQuestion(idx, field, value) {
    setForm((f) => {
      const questions = [...f.questions]
      questions[idx] = { ...questions[idx], [field]: value }
      return { ...f, questions }
    })
  }

  function updateOption(qIdx, optIdx, value) {
    setForm((f) => {
      const questions = [...f.questions]
      const options = [...questions[qIdx].options]
      options[optIdx] = value
      questions[qIdx] = { ...questions[qIdx], options }
      return { ...f, questions }
    })
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await adminApi.createQuiz({ ...form, topicId })
      showToast('Quiz created.', 'success')
      setForm(EMPTY_FORM)
      setShowForm(false)
      adminApi.listQuizzes(topicId).then((res) => setQuizzes(res.data))
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create quiz.', 'error')
    } finally {
      setSaving(false)
    }
  }

  async function remove(quiz) {
    if (!confirm(`Delete "${quiz.title}"?`)) return
    await adminApi.deleteQuiz(quiz.id)
    adminApi.listQuizzes(topicId).then((res) => setQuizzes(res.data))
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Quizzes</h1>
        <button onClick={() => setShowForm((v) => !v)} className="flex items-center gap-1 bg-eco-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-eco-700">
          <Plus size={16} /> New Quiz
        </button>
      </div>

      <select value={topicId} onChange={(e) => setTopicId(e.target.value)} className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm mb-6">
        {topics.map((t) => <option key={t.id} value={t.id}>{t.title.en}</option>)}
      </select>

      {showForm && (
        <Card className="mb-6">
          <form onSubmit={handleCreate} className="space-y-4">
            <input required placeholder="Quiz title" value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <div className="grid grid-cols-2 gap-3">
              <input type="number" min={1} value={form.coinsPerCorrectAnswer} onChange={(e) => setForm((f) => ({ ...f, coinsPerCorrectAnswer: Number(e.target.value) }))}
                placeholder="Coins per correct answer" className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
              <input type="number" min={1} value={form.xpPerCorrectAnswer} onChange={(e) => setForm((f) => ({ ...f, xpPerCorrectAnswer: Number(e.target.value) }))}
                placeholder="XP per correct answer" className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            </div>

            {form.questions.map((q, qIdx) => (
              <div key={qIdx} className="border border-gray-100 rounded-md p-3 space-y-2">
                <input required placeholder={`Question ${qIdx + 1}`} value={q.text} onChange={(e) => updateQuestion(qIdx, 'text', e.target.value)}
                  className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
                {q.options.map((opt, optIdx) => (
                  <div key={optIdx} className="flex items-center gap-2">
                    <input type="radio" checked={q.correctOptionIndex === optIdx} onChange={() => updateQuestion(qIdx, 'correctOptionIndex', optIdx)} />
                    <input required placeholder={`Option ${optIdx + 1}`} value={opt} onChange={(e) => updateOption(qIdx, optIdx, e.target.value)}
                      className="flex-1 border border-gray-200 rounded-md px-3 py-1.5 text-sm" />
                  </div>
                ))}
                <input placeholder="Explanation" value={q.explanation} onChange={(e) => updateQuestion(qIdx, 'explanation', e.target.value)}
                  className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
              </div>
            ))}

            <button type="button" onClick={() => setForm((f) => ({ ...f, questions: [...f.questions, EMPTY_QUESTION()] }))}
              className="text-sm text-eco-700 font-medium">
              + Add another question
            </button>

            <div className="flex gap-3 pt-2">
              <button type="submit" disabled={saving} className="bg-eco-600 text-white px-5 py-2 rounded-md text-sm font-medium hover:bg-eco-700 disabled:opacity-50">
                {saving ? 'Creating...' : 'Create Quiz'}
              </button>
              <button type="button" onClick={() => setShowForm(false)} className="text-sm text-gray-500">Cancel</button>
            </div>
          </form>
        </Card>
      )}

      {!quizzes ? <div className="flex justify-center py-16"><Spinner size={32} /></div> : (
        <div className="space-y-3">
          {quizzes.map((q) => (
            <Card key={q.id} className="flex items-center justify-between">
              <div>
                <h3 className="font-medium text-gray-800">{q.title}</h3>
                <p className="text-xs text-gray-400">{q.questions.length} questions</p>
              </div>
              <button onClick={() => remove(q)} className="text-red-400 hover:text-red-600"><Trash2 size={16} /></button>
            </Card>
          ))}
          {quizzes.length === 0 && <p className="text-gray-500">No quizzes for this topic yet.</p>}
        </div>
      )}
    </div>
  )
}
