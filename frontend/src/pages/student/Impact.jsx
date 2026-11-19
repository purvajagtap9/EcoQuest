import { useEffect, useState } from 'react'
import { TreePine, Recycle, Droplets, Zap, Bike, Trash2, Users } from 'lucide-react'
import { studentApi } from '../../api/student'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'

const ICONS = {
  TREES_PLANTED: <TreePine size={22} className="text-eco-700" />,
  WASTE_SEGREGATED_KG: <Recycle size={22} className="text-blue-600" />,
  PLASTIC_AVOIDED_KG: <Trash2 size={22} className="text-purple-600" />,
  WATER_SAVED_LITERS: <Droplets size={22} className="text-cyan-600" />,
  ENERGY_SAVED_KWH: <Zap size={22} className="text-yellow-600" />,
  SUSTAINABLE_TRIPS: <Bike size={22} className="text-orange-600" />,
  COMMUNITY_CLEANUPS: <Users size={22} className="text-pink-600" />,
}

const LABELS = {
  TREES_PLANTED: 'Trees Planted',
  WASTE_SEGREGATED_KG: 'Waste Segregated',
  PLASTIC_AVOIDED_KG: 'Plastic Avoided',
  WATER_SAVED_LITERS: 'Water Saved',
  ENERGY_SAVED_KWH: 'Energy Saved',
  SUSTAINABLE_TRIPS: 'Sustainable Trips',
  COMMUNITY_CLEANUPS: 'Community Cleanups',
}

export default function Impact() {
  const [summary, setSummary] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    studentApi
      .getImpact()
      .then((res) => setSummary(res.data))
      .catch(() => setError('Could not load your impact.'))
  }, [])

  if (error) return <div className="max-w-4xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!summary) return <div className="max-w-4xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-2">🌍 Your Environmental Impact</h1>
      <p className="text-xs text-gray-400 mb-6">{summary.disclaimer}</p>

      {summary.items.length === 0 ? (
        <p className="text-gray-500">Complete and get quests approved to start building your impact record.</p>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
          {summary.items.map((item) => (
            <Card key={item.type} className="text-center">
              <div className="flex justify-center mb-2">{ICONS[item.type] || <TreePine size={22} />}</div>
              <p className="text-2xl font-bold text-gray-800">{item.totalValue.toLocaleString()}</p>
              <p className="text-xs text-gray-500">{item.unit}</p>
              <p className="text-xs text-gray-400 mt-1">{LABELS[item.type] || item.type}</p>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
