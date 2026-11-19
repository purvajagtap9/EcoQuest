import { useEffect, useState } from 'react'
import { adminApi } from '../../api/admin'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

export default function AdminTeachers() {
  const { showToast } = useToast()
  const [page, setPage] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => { load() }, [])

  function load() {
    adminApi
      .listTeachers({ page: 0, size: 50, sort: 'name,asc' })
      .then((res) => setPage(res.data))
      .catch(() => setError('Could not load teachers.'))
  }

  async function toggleStatus(teacher) {
    try {
      await adminApi.setTeacherStatus(teacher.userId, !teacher.enabled)
      showToast(`${teacher.name} ${teacher.enabled ? 'disabled' : 'enabled'}.`, 'success')
      load()
    } catch {
      showToast('Could not update status.', 'error')
    }
  }

  async function remove(teacher) {
    if (!confirm(`Delete ${teacher.name}? This cannot be undone.`)) return
    try {
      await adminApi.deleteTeacher(teacher.userId)
      showToast('Teacher deleted.', 'success')
      load()
    } catch {
      showToast('Could not delete teacher.', 'error')
    }
  }

  if (error) return <div className="max-w-5xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!page) return <div className="max-w-5xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Teachers ({page.totalElements})</h1>

      <Card className="!p-0 overflow-x-auto">
        <table className="w-full text-sm min-w-[700px]">
          <thead className="bg-eco-50 text-gray-500 text-xs uppercase">
            <tr>
              <th className="text-left px-4 py-3">Name</th>
              <th className="text-left px-4 py-3">Email</th>
              <th className="text-left px-4 py-3">School</th>
              <th className="text-center px-4 py-3">Status</th>
              <th className="text-right px-4 py-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            {page.content.map((t) => (
              <tr key={t.userId} className="border-t border-gray-50">
                <td className="px-4 py-3 text-gray-800">{t.name}</td>
                <td className="px-4 py-3 text-gray-500">{t.email}</td>
                <td className="px-4 py-3 text-gray-500">{t.assignedSchoolId || '—'}</td>
                <td className="px-4 py-3 text-center">
                  <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${t.enabled ? 'bg-eco-100 text-eco-700' : 'bg-gray-100 text-gray-500'}`}>
                    {t.enabled ? 'Active' : 'Disabled'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right space-x-3">
                  <button onClick={() => toggleStatus(t)} className="text-xs font-medium text-blue-600 hover:underline">
                    {t.enabled ? 'Disable' : 'Enable'}
                  </button>
                  <button onClick={() => remove(t)} className="text-xs font-medium text-red-600 hover:underline">
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {page.content.length === 0 && <p className="text-gray-400 text-sm text-center py-8">No teachers yet.</p>}
      </Card>
    </div>
  )
}
