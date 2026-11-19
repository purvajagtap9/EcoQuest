import { useEffect, useState } from 'react'
import { Plus, Coins, Zap, Trash2 } from 'lucide-react'
import { adminApi } from '../../api/admin'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

const CATEGORIES = ['WASTE_MANAGEMENT', 'TREE_PLANTATION', 'WATER_CONSERVATION', 'ENERGY_CONSERVATION',
  'PLASTIC_FREE', 'SUSTAINABLE_TRANSPORT', 'COMMUNITY_CLEANUP', 'BIODIVERSITY', 'OTHER']
const DIFFICULTIES = ['EASY', 'MEDIUM', 'HARD']

const EMPTY_FORM = {
  title: '', description: '', category: 'WASTE_MANAGEMENT', difficulty: 'EASY',
  ecoCoinsReward: 50, xpReward: 30, proofRequired: true, requiredProofDescription: '',
  gpsRequired: false, maxSubmissionsPerStudent: 1, schoolId: '',
}

export default function AdminQuests() {
  const { showToast } = useToast()
  const [page, setPage] = useState(null)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)

  useEffect(() => { load() }, [])

  function load() {
    adminApi.listQuests({ page: 0, size: 50, sort: 'title,asc' })
      .then((res) => setPage(res.data)).catch(() => setError('Could not load quests.'))
  }

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSaving(true)
    try {
      const payload = { ...form, schoolId: form.schoolId || undefined }
      await adminApi.createQuest(payload)
      showToast('Quest created.', 'success')
      setForm(EMPTY_FORM)
      setShowForm(false)
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create quest.', 'error')
    } finally {
      setSaving(false)
    }
  }

  async function remove(quest) {
    if (!confirm(`Delete "${quest.title}"?`)) return
    try {
      await adminApi.deleteQuest(quest.id)
      showToast('Quest deleted.', 'success')
      load()
    } catch {
      showToast('Could not delete quest.', 'error')
    }
  }

  if (error) return <div className="max-w-4xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!page) return <div className="max-w-4xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Quests ({page.totalElements})</h1>
        <button onClick={() => setShowForm((v) => !v)} className="flex items-center gap-1 bg-eco-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-eco-700">
          <Plus size={16} /> New Quest
        </button>
      </div>

      {showForm && (
        <Card className="mb-6">
          <form onSubmit={handleCreate} className="space-y-3">
            <input required placeholder="Title" value={form.title} onChange={(e) => update('title', e.target.value)}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <textarea required placeholder="Description" rows={2} value={form.description} onChange={(e) => update('description', e.target.value)}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <div className="grid grid-cols-2 gap-3">
              <select value={form.category} onChange={(e) => update('category', e.target.value)} className="border border-gray-200 rounded-md px-3 py-2 text-sm">
                {CATEGORIES.map((c) => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}
              </select>
              <select value={form.difficulty} onChange={(e) => update('difficulty', e.target.value)} className="border border-gray-200 rounded-md px-3 py-2 text-sm">
                {DIFFICULTIES.map((d) => <option key={d} value={d}>{d}</option>)}
              </select>
              <input type="number" min={1} placeholder="Eco-Coins reward" value={form.ecoCoinsReward}
                onChange={(e) => update('ecoCoinsReward', Number(e.target.value))} className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
              <input type="number" min={1} placeholder="XP reward" value={form.xpReward}
                onChange={(e) => update('xpReward', Number(e.target.value))} className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            </div>
            <input placeholder="School ID (leave blank for all schools)" value={form.schoolId} onChange={(e) => update('schoolId', e.target.value)}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <label className="flex items-center gap-2 text-sm text-gray-600">
              <input type="checkbox" checked={form.gpsRequired} onChange={(e) => update('gpsRequired', e.target.checked)} />
              Require GPS location
            </label>
            <div className="flex gap-3">
              <button type="submit" disabled={saving} className="bg-eco-600 text-white px-5 py-2 rounded-md text-sm font-medium hover:bg-eco-700 disabled:opacity-50">
                {saving ? 'Creating...' : 'Create Quest'}
              </button>
              <button type="button" onClick={() => setShowForm(false)} className="text-sm text-gray-500">Cancel</button>
            </div>
          </form>
        </Card>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {page.content.map((q) => (
          <Card key={q.id}>
            <div className="flex items-start justify-between mb-1">
              <h3 className="font-semibold text-gray-800">{q.title}</h3>
              <button onClick={() => remove(q)} className="text-red-400 hover:text-red-600"><Trash2 size={14} /></button>
            </div>
            <p className="text-sm text-gray-500 line-clamp-2 mb-3">{q.description}</p>
            <div className="flex items-center gap-4 text-sm">
              <span className="flex items-center gap-1 text-yellow-600"><Coins size={14} /> {q.ecoCoinsReward}</span>
              <span className="flex items-center gap-1 text-eco-600"><Zap size={14} /> {q.xpReward} XP</span>
              <span className="text-xs text-gray-400 ml-auto">{q.schoolId ? 'School-specific' : 'All schools'}</span>
            </div>
          </Card>
        ))}
      </div>
      {page.content.length === 0 && <p className="text-gray-500">No quests yet.</p>}
    </div>
  )
}
