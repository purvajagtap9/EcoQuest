import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Coins, Zap, MapPin, Camera, ExternalLink } from 'lucide-react'
import { studentApi } from '../../api/student'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

export default function QuestDetail() {
  const { questId } = useParams()
  const navigate = useNavigate()
  const { showToast } = useToast()

  const [detail, setDetail] = useState(null)
  const [error, setError] = useState('')
  const [showForm, setShowForm] = useState(false)

  const [image, setImage] = useState(null)
  const [imagePreview, setImagePreview] = useState(null)
  const [description, setDescription] = useState('')
  const [coords, setCoords] = useState(null)
  const [locating, setLocating] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  useEffect(() => {
    studentApi
      .getQuest(questId)
      .then((res) => setDetail(res.data))
      .catch(() => setError('Could not load this quest.'))
  }, [questId])

  function handleImageChange(e) {
    const file = e.target.files?.[0]
    if (!file) return
    setImage(file)
    setImagePreview(URL.createObjectURL(file))
  }

  function captureLocation() {
    if (!navigator.geolocation) {
      setFormError('Your browser does not support location capture.')
      return
    }
    setLocating(true)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setCoords({ latitude: pos.coords.latitude, longitude: pos.coords.longitude })
        setLocating(false)
      },
      () => {
        setFormError('Could not get your location. Please allow location access and try again.')
        setLocating(false)
      }
    )
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setFormError('')

    const quest = detail.quest
    if (quest.proofRequired && !image) {
      setFormError('This quest requires a photo as proof.')
      return
    }
    if (quest.gpsRequired && !coords) {
      setFormError('This quest requires your location — tap "Capture My Location" first.')
      return
    }

    const formData = new FormData()
    if (image) formData.append('image', image)
    if (description) formData.append('description', description)
    if (coords) {
      formData.append('latitude', coords.latitude)
      formData.append('longitude', coords.longitude)
    }

    setSubmitting(true)
    try {
      await studentApi.submitQuestProof(questId, formData)
      showToast('Proof submitted! Your teacher will review it soon. 🌱', 'success')
      navigate('/student/submissions')
    } catch (err) {
      setFormError(err.response?.data?.message || 'Could not submit your proof.')
    } finally {
      setSubmitting(false)
    }
  }

  if (error) return <div className="max-w-2xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!detail) return <div className="max-w-2xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  const { quest, canSubmit, blockReason, mySubmissionCount } = detail

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <Card className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800 mb-2">{quest.title}</h1>
        <p className="text-gray-600 mb-4">{quest.description}</p>

        <div className="flex items-center gap-4 mb-4">
          <span className="flex items-center gap-1 text-yellow-600 font-semibold">
            <Coins size={18} /> {quest.ecoCoinsReward} Eco-Coins
          </span>
          <span className="flex items-center gap-1 text-eco-600 font-semibold">
            <Zap size={18} /> {quest.xpReward} XP
          </span>
        </div>

        {quest.requiredProofDescription && (
          <p className="text-sm text-gray-500 mb-2">
            <strong>Proof needed:</strong> {quest.requiredProofDescription}
          </p>
        )}

        {quest.gpsRequired && quest.targetLocationMapUrl && (
          <a
            href={quest.targetLocationMapUrl}
            target="_blank"
            rel="noreferrer"
            className="inline-flex items-center gap-1 text-sm text-eco-700 mb-2"
          >
            <MapPin size={14} /> View target location on map <ExternalLink size={12} />
          </a>
        )}

        <p className="text-xs text-gray-400 mt-2">
          Submissions used: {mySubmissionCount} / {quest.maxSubmissionsPerStudent}
        </p>

        {!canSubmit && <p className="text-sm text-red-500 mt-3">{blockReason}</p>}

        {canSubmit && !showForm && (
          <button
            onClick={() => setShowForm(true)}
            className="mt-4 bg-eco-600 text-white px-6 py-2.5 rounded-md font-medium hover:bg-eco-700"
          >
            Start Quest
          </button>
        )}
      </Card>

      {showForm && canSubmit && (
        <Card>
          <h2 className="font-semibold text-gray-700 mb-4">Submit Proof</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-600 mb-2">Photo</label>
              <label className="flex flex-col items-center justify-center border-2 border-dashed border-gray-200 rounded-lg py-8 cursor-pointer hover:border-eco-300">
                {imagePreview ? (
                  <img src={imagePreview} alt="Preview" className="max-h-48 rounded-md" />
                ) : (
                  <>
                    <Camera size={28} className="text-gray-300 mb-2" />
                    <span className="text-sm text-gray-400">Tap to take or upload a photo</span>
                  </>
                )}
                <input type="file" accept="image/*" capture="environment" onChange={handleImageChange} className="hidden" />
              </label>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-600 mb-1">Description</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                rows={3}
                className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-eco-400"
                placeholder="What did you do?"
              />
            </div>

            {quest.gpsRequired && (
              <div>
                <button
                  type="button"
                  onClick={captureLocation}
                  disabled={locating}
                  className="flex items-center gap-2 border border-eco-200 text-eco-700 px-4 py-2 rounded-md text-sm font-medium hover:bg-eco-50"
                >
                  <MapPin size={16} /> {locating ? 'Locating...' : coords ? 'Location Captured ✓' : 'Capture My Location'}
                </button>
                {coords && (
                  <p className="text-xs text-gray-400 mt-1">
                    {coords.latitude.toFixed(5)}, {coords.longitude.toFixed(5)}
                  </p>
                )}
              </div>
            )}

            {formError && <p className="text-sm text-red-600">{formError}</p>}

            <button
              type="submit"
              disabled={submitting}
              className="w-full bg-eco-600 text-white rounded-md py-3 font-medium hover:bg-eco-700 disabled:opacity-50"
            >
              {submitting ? 'Submitting...' : 'Submit Proof'}
            </button>
          </form>
        </Card>
      )}
    </div>
  )
}
