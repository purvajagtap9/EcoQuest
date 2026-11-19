import { useEffect, useState } from 'react'
import { Plus, Trash2, ChevronDown, ChevronUp } from 'lucide-react'
import { adminApi } from '../../api/admin'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

const EMPTY_TOPIC = { titleEn: '', titleHi: '', titleMr: '', descriptionEn: '', orderIndex: 1, published: true }
const EMPTY_LESSON = { titleEn: '', bodyEn: '', localContextNoteEn: '', orderIndex: 1 }

export default function AdminTopics() {
  const { showToast } = useToast()
  const [topics, setTopics] = useState(null)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(EMPTY_TOPIC)
  const [saving, setSaving] = useState(false)
  const [expandedTopic, setExpandedTopic] = useState(null)
  const [lessons, setLessons] = useState({})
  const [lessonForm, setLessonForm] = useState(EMPTY_LESSON)

  useEffect(() => { load() }, [])

  function load() {
    adminApi.listTopics().then((res) => setTopics(res.data)).catch(() => setError('Could not load topics.'))
  }

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleCreateTopic(e) {
    e.preventDefault()
    setSaving(true)
    try {
      await adminApi.createTopic({
        title: { en: form.titleEn, hi: form.titleHi || undefined, mr: form.titleMr || undefined },
        description: { en: form.descriptionEn },
        orderIndex: Number(form.orderIndex),
        published: form.published,
      })
      showToast('Topic created.', 'success')
      setForm(EMPTY_TOPIC)
      setShowForm(false)
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create topic.', 'error')
    } finally {
      setSaving(false)
    }
  }

  async function removeTopic(topic) {
    if (!confirm(`Delete "${topic.title.en}" and all its lessons?`)) return
    try {
      await adminApi.deleteTopic(topic.id)
      showToast('Topic deleted.', 'success')
      load()
    } catch {
      showToast('Could not delete topic.', 'error')
    }
  }

  function toggleExpand(topic) {
    if (expandedTopic === topic.id) {
      setExpandedTopic(null)
      return
    }
    setExpandedTopic(topic.id)
    if (!lessons[topic.id]) {
      adminApi.listLessons(topic.id).then((res) => setLessons((l) => ({ ...l, [topic.id]: res.data })))
    }
  }

  async function handleCreateLesson(e, topicId) {
    e.preventDefault()
    try {
      await adminApi.createLesson({
        topicId,
        title: { en: lessonForm.titleEn },
        body: { en: lessonForm.bodyEn },
        localContextNote: lessonForm.localContextNoteEn ? { en: lessonForm.localContextNoteEn } : undefined,
        orderIndex: Number(lessonForm.orderIndex),
      })
      showToast('Lesson added.', 'success')
      setLessonForm(EMPTY_LESSON)
      adminApi.listLessons(topicId).then((res) => setLessons((l) => ({ ...l, [topicId]: res.data })))
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not add lesson.', 'error')
    }
  }

  async function removeLesson(topicId, lessonId) {
    if (!confirm('Delete this lesson?')) return
    await adminApi.deleteLesson(lessonId)
    adminApi.listLessons(topicId).then((res) => setLessons((l) => ({ ...l, [topicId]: res.data })))
  }

  if (error) return <div className="max-w-4xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!topics) return <div className="max-w-4xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Topics ({topics.length})</h1>
        <button onClick={() => setShowForm((v) => !v)} className="flex items-center gap-1 bg-eco-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-eco-700">
          <Plus size={16} /> New Topic
        </button>
      </div>

      {showForm && (
        <Card className="mb-6">
          <form onSubmit={handleCreateTopic} className="space-y-3">
            <input required placeholder="Title (English)" value={form.titleEn} onChange={(e) => update('titleEn', e.target.value)}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <div className="grid grid-cols-2 gap-3">
              <input placeholder="Title (Hindi, optional)" value={form.titleHi} onChange={(e) => update('titleHi', e.target.value)}
                className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
              <input placeholder="Title (Marathi, optional)" value={form.titleMr} onChange={(e) => update('titleMr', e.target.value)}
                className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            </div>
            <textarea required placeholder="Description" rows={2} value={form.descriptionEn} onChange={(e) => update('descriptionEn', e.target.value)}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <input type="number" min={1} placeholder="Order" value={form.orderIndex} onChange={(e) => update('orderIndex', e.target.value)}
              className="border border-gray-200 rounded-md px-3 py-2 text-sm w-24" />
            <div className="flex gap-3">
              <button type="submit" disabled={saving} className="bg-eco-600 text-white px-5 py-2 rounded-md text-sm font-medium hover:bg-eco-700 disabled:opacity-50">
                {saving ? 'Creating...' : 'Create Topic'}
              </button>
              <button type="button" onClick={() => setShowForm(false)} className="text-sm text-gray-500">Cancel</button>
            </div>
          </form>
        </Card>
      )}

      <div className="space-y-3">
        {topics.map((t) => (
          <Card key={t.id} className="!p-0">
            <div className="flex items-center justify-between p-4">
              <div>
                <h3 className="font-semibold text-gray-800">{t.title.en}</h3>
                <p className="text-sm text-gray-500">{t.description.en}</p>
              </div>
              <div className="flex items-center gap-3">
                <button onClick={() => removeTopic(t)} className="text-red-500 hover:text-red-700"><Trash2 size={16} /></button>
                <button onClick={() => toggleExpand(t)} className="text-gray-400">
                  {expandedTopic === t.id ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                </button>
              </div>
            </div>

            {expandedTopic === t.id && (
              <div className="border-t border-gray-100 p-4 bg-gray-50">
                <h4 className="text-sm font-medium text-gray-600 mb-2">Lessons</h4>
                <div className="space-y-2 mb-4">
                  {lessons[t.id]?.map((l) => (
                    <div key={l.id} className="flex items-center justify-between bg-white rounded-md px-3 py-2 text-sm">
                      <span>{l.title.en}</span>
                      <button onClick={() => removeLesson(t.id, l.id)} className="text-red-400 hover:text-red-600">
                        <Trash2 size={14} />
                      </button>
                    </div>
                  ))}
                  {lessons[t.id]?.length === 0 && <p className="text-xs text-gray-400">No lessons yet.</p>}
                </div>
                <form onSubmit={(e) => handleCreateLesson(e, t.id)} className="space-y-2">
                  <input required placeholder="Lesson title" value={lessonForm.titleEn}
                    onChange={(e) => setLessonForm((f) => ({ ...f, titleEn: e.target.value }))}
                    className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
                  <textarea required placeholder="Lesson body (short!)" rows={2} value={lessonForm.bodyEn}
                    onChange={(e) => setLessonForm((f) => ({ ...f, bodyEn: e.target.value }))}
                    className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
                  <input placeholder="Local context note (optional)" value={lessonForm.localContextNoteEn}
                    onChange={(e) => setLessonForm((f) => ({ ...f, localContextNoteEn: e.target.value }))}
                    className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
                  <button type="submit" className="bg-eco-600 text-white px-4 py-1.5 rounded-md text-xs font-medium hover:bg-eco-700">
                    Add Lesson
                  </button>
                </form>
              </div>
            )}
          </Card>
        ))}
      </div>
    </div>
  )
}
