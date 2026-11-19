import { useEffect, useState } from 'react'
import { TreePine, Recycle, Droplets, Zap, Bike, Trash2, Users } from 'lucide-react'
import { adminApi } from '../../api/admin'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

const ICONS = {
  TREES_PLANTED: <TreePine size={22} className="text-eco-700" />,
  WASTE_SEGREGATED_KG: <Recycle size={22} className="text-blue-600" />,
  PLASTIC_AVOIDED_KG: <Trash2 size={22} className="text-purple-600" />,
  WATER_SAVED_LITERS: <Droplets size={22} className="text-cyan-600" />,
  ENERGY_SAVED_KWH: <Zap size={22} className="text-yellow-600" />,
  SUSTAINABLE_TRIPS: <Bike size={22} className="text-orange-600" />,
  COMMUNITY_CLEANUPS: <Users size={22} className="text-pink-600" />,
}

export default function AdminAnalytics() {
  const { showToast } = useToast()
  const [impact, setImpact] = useState(null)
  const [formulas, setFormulas] = useState(null)
  const [error, setError] = useState('')
  const [editing, setEditing] = useState(null)
  const [editValue, setEditValue] = useState('')

  useEffect(() => { load() }, [])

  function load() {
    adminApi.getGlobalImpact().then((res) => setImpact(res.data)).catch(() => setError('Could not load impact data.'))
    adminApi.listImpactFormulas().then((res) => setFormulas(res.data)).catch(() => {})
  }

  function startEdit(formula) {
    setEditing(formula.category)
    setEditValue(formula.valuePerCompletion ?? '')
  }

  async function saveFormula(formula) {
    try {
      await adminApi.upsertImpactFormula({
        category: formula.category,
        impactType: formula.impactType,
        valuePerCompletion: Number(editValue),
        unit: formula.unit,
      })
      showToast('Formula updated.', 'success')
      setEditing(null)
      load()
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not update formula.', 'error')
    }
  }

  if (error) return <div className="max-w-4xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!impact) return <div className="max-w-4xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-2">🌍 Platform Impact</h1>
      <p className="text-xs text-gray-400 mb-6">{impact.disclaimer}</p>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
        {impact.items.map((item) => (
          <Card key={item.type} className="text-center">
            <div className="flex justify-center mb-2">{ICONS[item.type] || <TreePine size={22} />}</div>
            <p className="text-xl font-bold text-gray-800">{item.totalValue.toLocaleString()}</p>
            <p className="text-xs text-gray-500">{item.unit}</p>
          </Card>
        ))}
        {impact.items.length === 0 && <p className="text-gray-500 col-span-full">No impact recorded yet.</p>}
      </div>

      <h2 className="text-lg font-semibold text-gray-800 mb-3">Impact Formulas (configurable)</h2>
      <Card className="!p-0 overflow-x-auto">
        <table className="w-full text-sm min-w-[600px]">
          <thead className="bg-eco-50 text-gray-500 text-xs uppercase">
            <tr>
              <th className="text-left px-4 py-3">Quest Category</th>
              <th className="text-left px-4 py-3">Metric</th>
              <th className="text-right px-4 py-3">Value / Completion</th>
              <th className="text-right px-4 py-3">Action</th>
            </tr>
          </thead>
          <tbody>
            {formulas?.map((f) => (
              <tr key={f.category} className="border-t border-gray-50">
                <td className="px-4 py-3 text-gray-800">{f.category.replace(/_/g, ' ')}</td>
                <td className="px-4 py-3 text-gray-500">{f.impactType ? f.impactType.replace(/_/g, ' ') : '—'}</td>
                <td className="px-4 py-3 text-right">
                  {editing === f.category ? (
                    <input
                      type="number"
                      value={editValue}
                      onChange={(e) => setEditValue(e.target.value)}
                      className="w-20 border border-gray-200 rounded-md px-2 py-1 text-sm text-right"
                      autoFocus
                    />
                  ) : (
                    <span>{f.valuePerCompletion ?? '—'} {f.unit}</span>
                  )}
                  {f.isDefault && <span className="text-[10px] text-gray-400 ml-1">(default)</span>}
                </td>
                <td className="px-4 py-3 text-right">
                  {f.impactType && (editing === f.category ? (
                    <button onClick={() => saveFormula(f)} className="text-xs font-medium text-eco-600 hover:underline">Save</button>
                  ) : (
                    <button onClick={() => startEdit(f)} className="text-xs font-medium text-blue-600 hover:underline">Edit</button>
                  ))}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
