import { useEffect, useState } from 'react'
import { Plus, Trash2 } from 'lucide-react'
import { adminApi } from '../../api/admin'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

const EMPTY_FORM = { name: '', city: '', state: '', campusLatitude: '', campusLongitude: '' }

export default function AdminSchools() {
  const { showToast } = useToast()
  const [page, setPage] = useState(null)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [saving, setSaving] = useState(false)

  useEffect(() => { load() }, [])

  function load() {
    adminApi
      .listSchools({ page: 0, size: 50, sort: 'name,asc' })
      .then((res) => setPage(res.data))
      .catch(() => setError('Could not load schools.'))
  }

  function update(field, value) {
    setForm((f) => ({ ...f, [field]: value }))
  }

  async function handleCreate(e) {
    e.preventDefault()
    setSaving(true)
    try {
      const payload = {
        name: form.name,
        city: form.city,
        state: form.state,
        campusLatitude: form.campusLatitude ? Number(form.campusLatitude) : undefined,
        campusLongitude: form.campusLongitude ? Number(form.campusLongitude) : undefined,
      }
      await adminApi.createSchool(payload)
      showToast('School created.', 'success')
      setForm(EMPTY_FORM)
      setShowForm(false)
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not create school.', 'error')
    } finally {
      setSaving(false)
    }
  }

  async function remove(school) {
    if (!confirm(`Delete ${school.name}?`)) return
    try {
      await adminApi.deleteSchool(school.id)
      showToast('School deleted.', 'success')
      load()
    } catch {
      showToast('Could not delete school.', 'error')
    }
  }

  if (error) return <div className="max-w-4xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!page) return <div className="max-w-4xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Schools ({page.totalElements})</h1>
        <button
          onClick={() => setShowForm((v) => !v)}
          className="flex items-center gap-1 bg-eco-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-eco-700"
        >
          <Plus size={16} /> New School
        </button>
      </div>

      {showForm && (
        <Card className="mb-6">
          <form onSubmit={handleCreate} className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <input required placeholder="School name" value={form.name} onChange={(e) => update('name', e.target.value)}
              className="border border-gray-200 rounded-md px-3 py-2 text-sm sm:col-span-2" />
            <input required placeholder="City" value={form.city} onChange={(e) => update('city', e.target.value)}
              className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <input required placeholder="State" value={form.state} onChange={(e) => update('state', e.target.value)}
              className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <input placeholder="Campus latitude (optional)" value={form.campusLatitude} onChange={(e) => update('campusLatitude', e.target.value)}
              className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <input placeholder="Campus longitude (optional)" value={form.campusLongitude} onChange={(e) => update('campusLongitude', e.target.value)}
              className="border border-gray-200 rounded-md px-3 py-2 text-sm" />
            <div className="sm:col-span-2 flex gap-3">
              <button type="submit" disabled={saving} className="bg-eco-600 text-white px-5 py-2 rounded-md text-sm font-medium hover:bg-eco-700 disabled:opacity-50">
                {saving ? 'Creating...' : 'Create School'}
              </button>
              <button type="button" onClick={() => setShowForm(false)} className="text-sm text-gray-500">Cancel</button>
            </div>
          </form>
        </Card>
      )}

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {page.content.map((s) => (
          <Card key={s.id} className="flex items-start justify-between">
            <div>
              <h3 className="font-semibold text-gray-800">{s.name}</h3>
              <p className="text-sm text-gray-500">{s.city}, {s.state}</p>
              <p className="text-xs text-gray-400 mt-1">ID: {s.id}</p>
            </div>
            <button onClick={() => remove(s)} className="text-red-500 hover:text-red-700">
              <Trash2 size={16} />
            </button>
          </Card>
        ))}
      </div>
      {page.content.length === 0 && <p className="text-gray-500">No schools yet.</p>}
    </div>
  )
}
