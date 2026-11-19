import { useEffect, useState } from 'react'
import { Plus, Trash2 } from 'lucide-react'
import { adminApi } from '../../api/admin'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

const CRITERIA_TYPES = ['QUESTS_COMPLETED_TOTAL', 'QUESTS_COMPLETED_IN_CATEGORY', 'STREAK_DAYS', 'ECO_COINS_EARNED_TOTAL', 'QUIZZES_COMPLETED_TOTAL']
const CATEGORIES = ['WASTE_MANAGEMENT', 'TREE_PLANTATION', 'WATER_CONSERVATION', 'ENERGY_CONSERVATION',
  'PLASTIC_FREE', 'SUSTAINABLE_TRANSPORT', 'COMMUNITY_CLEANUP', 'BIODIVERSITY', 'OTHER']

const EMPTY_FORM = {
  code: '', name: '', description: '', icon: '🏅', criteriaType: 'QUESTS_COMPLETED_TOTAL',
  criteriaThreshold: 1, criteriaCategory: '', ecoCoinsReward: 25, xpReward: 20,
}

export default function AdminBadges() {
  const { showToast } = useToast()
  const [badges, setBadges] = useState(null)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)

  useEffect(() => { load() }, [])

  function load() {
    adminApi.listBadges().then((res) => setBadges(res.data)).catch(() => setError('Could not load badges.'))
  }

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSaving(true)
    try {
      const payload = { ...form, criteriaThreshold: Number(form.criteriaThreshold), criteriaCategory: form.criteriaCategory || undefined }
      await adminApi.createBadge(payload)
      showToast('Badge created.', 'success')
      setForm(EMPTY_FORM)
      setShowForm(false)
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create badge.', 'error')
    } finally {
      setSaving(false)
    }
  }

  async function remove(badge) {
    if (!confirm(`Delete "${badge.name}"?`)) return
    try {
      await adminApi.deleteBadge(badge.id)
      showToast('Badge deleted.', 'success')
      load()
    } catch {
      showToast('Could not delete badge.', 'error')
    }
  }

  if (error) return <div className="max-w-4xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!badges) return <div className="max-w-4xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Badges ({badges.length})</h1>
        <button onClick={() => setShowForm((v) => !v)} className="flex items-center gap-1 bg-eco-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-eco-700">
          <Plus size={16} /> New Badge
        </button>
      </div>

      {showForm && (
        <Card className="mb-6">
          <form onSubmit={handleCreate} className="space-y-3">
            <div className="grid grid-cols-3 gap-3">
              <input required placeholder="Code (e.g. FIRST_STEP)" value={form.code} onChange={(e) => update('code', e.target.value)}
                className="border border-gray-200 rounded-md px-3 py-2 text-sm col-span-2" />
              <input placeholder="Icon (emoji)" value={form.icon} onChange={(e) => update('icon', e.target.value)}
                className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            </div>
            <input required placeholder="Name" value={form.name} onChange={(e) => update('name', e.target.value)}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <textarea required placeholder="Description" rows={2} value={form.description} onChange={(e) => update('description', e.target.value)}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <div className="grid grid-cols-2 gap-3">
              <select value={form.criteriaType} onChange={(e) => update('criteriaType', e.target.value)} className="border border-gray-200 rounded-md px-3 py-2 text-sm">
                {CRITERIA_TYPES.map((c) => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
              </select>
              <input type="number" min={1} placeholder="Threshold" value={form.criteriaThreshold}
                onChange={(e) => update('criteriaThreshold', e.target.value)} className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            </div>
            {form.criteriaType === 'QUESTS_COMPLETED_IN_CATEGORY' && (
              <select value={form.criteriaCategory} onChange={(e) => update('criteriaCategory', e.target.value)} className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm">
                <option value="">Select category</option>
                {CATEGORIES.map((c) => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}
              </select>
            )}
            <div className="grid grid-cols-2 gap-3">
              <input type="number" min={0} placeholder="Eco-Coins reward" value={form.ecoCoinsReward}
                onChange={(e) => update('ecoCoinsReward', Number(e.target.value))} className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
              <input type="number" min={0} placeholder="XP reward" value={form.xpReward}
                onChange={(e) => update('xpReward', Number(e.target.value))} className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            </div>
            <div className="flex gap-3">
              <button type="submit" disabled={saving} className="bg-eco-600 text-white px-5 py-2 rounded-md text-sm font-medium hover:bg-eco-700 disabled:opacity-50">
                {saving ? 'Creating...' : 'Create Badge'}
              </button>
              <button type="button" onClick={() => setShowForm(false)} className="text-sm text-gray-500">Cancel</button>
            </div>
          </form>
        </Card>
      )}

      <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
        {badges.map((b) => (
          <Card key={b.id} className="text-center relative">
            <button onClick={() => remove(b)} className="absolute top-2 right-2 text-red-400 hover:text-red-600">
              <Trash2 size={14} />
            </button>
            <div className="text-3xl mb-2">{b.icon}</div>
            <h3 className="font-semibold text-sm text-gray-800">{b.name}</h3>
            <p className="text-xs text-gray-500 mt-1">{b.description}</p>
            <p className="text-[11px] text-gray-400 mt-2">{b.criteriaType.replace(/_/g, ' ')} ≥ {b.criteriaThreshold}</p>
          </Card>
        ))}
      </div>
      {badges.length === 0 && <p className="text-gray-500">No badges yet.</p>}
    </div>
  )
}
