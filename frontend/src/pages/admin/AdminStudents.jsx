import { useEffect, useState } from 'react'
import { adminApi } from '../../api/admin'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

export default function AdminStudents() {
  const { showToast } = useToast()
  const [page, setPage] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => { load() }, [])

  function load() {
    adminApi
      .listStudents({ page: 0, size: 50, sort: 'name,asc' })
      .then((res) => setPage(res.data))
      .catch(() => setError('Could not load students.'))
  }

  async function toggleStatus(student) {
    try {
      await adminApi.setStudentStatus(student.userId, !student.enabled)
      showToast(`${student.name} ${student.enabled ? 'disabled' : 'enabled'}.`, 'success')
      load()
    } catch {
      showToast('Could not update status.', 'error')
    }
  }

  async function remove(student) {
    if (!confirm(`Delete ${student.name}? This cannot be undone.`)) return
    try {
      await adminApi.deleteStudent(student.userId)
      showToast('Student deleted.', 'success')
      load()
    } catch {
      showToast('Could not delete student.', 'error')
    }
  }

  if (error) return <div className="max-w-5xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!page) return <div className="max-w-5xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Students ({page.totalElements})</h1>

      <Card className="!p-0 overflow-x-auto">
        <table className="w-full text-sm min-w-[700px]">
          <thead className="bg-eco-50 text-gray-500 text-xs uppercase">
            <tr>
              <th className="text-left px-4 py-3">Name</th>
              <th className="text-left px-4 py-3">Email</th>
              <th className="text-right px-4 py-3">Eco-Coins</th>
              <th className="text-center px-4 py-3">Status</th>
              <th className="text-right px-4 py-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            {page.content.map((s) => (
              <tr key={s.userId} className="border-t border-gray-50">
                <td className="px-4 py-3 text-gray-800">{s.name}</td>
                <td className="px-4 py-3 text-gray-500">{s.email}</td>
                <td className="px-4 py-3 text-right font-medium text-yellow-600">{s.ecoCoins}</td>
                <td className="px-4 py-3 text-center">
                  <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${s.enabled ? 'bg-eco-100 text-eco-700' : 'bg-gray-100 text-gray-500'}`}>
                    {s.enabled ? 'Active' : 'Disabled'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right space-x-3">
                  <button onClick={() => toggleStatus(s)} className="text-xs font-medium text-blue-600 hover:underline">
                    {s.enabled ? 'Disable' : 'Enable'}
                  </button>
                  <button onClick={() => remove(s)} className="text-xs font-medium text-red-600 hover:underline">
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {page.content.length === 0 && <p className="text-gray-400 text-sm text-center py-8">No students yet.</p>}
      </Card>
    </div>
  )
}
