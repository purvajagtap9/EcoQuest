import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { ArrowLeft, MapPin, ExternalLink, Bot, CheckCircle2, XCircle } from 'lucide-react'
import { teacherApi } from '../../api/teacher'
import Card from '../../components/Card'
import Spinner from '../../components/Spinner'
import { useToast } from '../../context/ToastContext'

const AI_BADGE = {
  LIKELY_VALID: 'bg-eco-100 text-eco-700',
  LIKELY_INVALID: 'bg-red-100 text-red-700',
  UNCERTAIN: 'bg-yellow-100 text-yellow-700',
}

export default function TeacherSubmissionDetail() {
  const { submissionId } = useParams()
  const navigate = useNavigate()
  const { showToast } = useToast()

  const [detail, setDetail] = useState(null)
  const [error, setError] = useState('')
  const [showRejectForm, setShowRejectForm] = useState(false)
  const [rejectionReason, setRejectionReason] = useState('')
  const [acting, setActing] = useState(false)

  useEffect(() => {
    teacherApi
      .getSubmission(submissionId)
      .then((res) => setDetail(res.data))
      .catch(() => setError('Could not load this submission.'))
  }, [submissionId])

  async function handleApprove() {
    setActing(true)
    try {
      await teacherApi.approveSubmission(submissionId)
      showToast('Submission approved! Eco-Coins awarded. 🌳', 'success')
      navigate('/teacher/submissions')
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not approve.', 'error')
    } finally {
      setActing(false)
    }
  }

  async function handleReject(e) {
    e.preventDefault()
    setActing(true)
    try {
      await teacherApi.rejectSubmission(submissionId, rejectionReason)
      showToast('Submission rejected.', 'info')
      navigate('/teacher/submissions')
    } catch (err) {
      showToast(err.response?.data?.message || 'Could not reject.', 'error')
    } finally {
      setActing(false)
    }
  }

  if (error) return <div className="max-w-2xl mx-auto px-4 py-10 text-red-600">{error}</div>
  if (!detail) return <div className="max-w-2xl mx-auto px-4 py-16 flex justify-center"><Spinner size={32} /></div>

  const isDecided = detail.status === 'APPROVED' || detail.status === 'REJECTED'

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <Link to="/teacher/submissions" className="inline-flex items-center gap-1 text-sm text-eco-700 mb-4">
        <ArrowLeft size={16} /> Back to Submissions
      </Link>

      <Card className="mb-4">
        <h1 className="text-xl font-bold text-gray-800 mb-1">{detail.questTitle}</h1>
        <p className="text-sm text-gray-500 mb-1">{detail.questDescription}</p>
        <p className="text-sm text-gray-700 font-medium">
          Student: {detail.studentName} · Reward: {detail.ecoCoinsReward} Eco-Coins / {detail.xpReward} XP
        </p>
      </Card>

      {detail.imageUrl && (
        <Card className="mb-4">
          <img
            src={`${import.meta.env.VITE_API_URL || 'http://localhost:8080'}${detail.imageUrl}`}
            alt="Evidence"
            className="w-full rounded-md max-h-96 object-contain bg-gray-50"
          />
          {detail.description && <p className="text-sm text-gray-600 mt-3">"{detail.description}"</p>}
        </Card>
      )}

      {detail.aiRecommendation && (
        <Card className="mb-4">
          <div className="flex items-center gap-2 mb-2">
            <Bot size={18} className="text-blue-600" />
            <h2 className="font-semibold text-gray-700">AI Verification (advisory)</h2>
            <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${AI_BADGE[detail.aiRecommendation]}`}>
              {detail.aiRecommendation.replace('_', ' ')}
            </span>
          </div>
          <p className="text-sm text-gray-600 mb-1">{detail.aiReason}</p>
          <p className="text-xs text-gray-400">Confidence: {Math.round(detail.aiConfidence * 100)}%</p>
        </Card>
      )}

      {detail.submittedLatitude != null && (
        <Card className="mb-4">
          <div className="flex items-center gap-2 mb-2">
            <MapPin size={18} className={detail.withinAllowedRadius === false ? 'text-red-500' : 'text-eco-600'} />
            <h2 className="font-semibold text-gray-700">Location</h2>
          </div>
          <p className="text-sm text-gray-600">
            {detail.submittedLatitude.toFixed(5)}, {detail.submittedLongitude.toFixed(5)}
            {detail.distanceFromTargetMeters != null && (
              <span className="text-gray-400"> · {Math.round(detail.distanceFromTargetMeters)}m from target</span>
            )}
          </p>
          {detail.withinAllowedRadius === false && (
            <p className="text-xs text-red-500 mt-1">⚠ Outside the allowed radius for this quest</p>
          )}
          <a href={detail.submissionMapUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-sm text-eco-700 mt-2">
            View on map <ExternalLink size={12} />
          </a>
        </Card>
      )}

      {isDecided ? (
        <Card>
          <p className={`font-medium ${detail.status === 'APPROVED' ? 'text-eco-700' : 'text-red-600'}`}>
            This submission was {detail.status.toLowerCase()}{detail.adminOverride ? ' (by admin override)' : ''}.
          </p>
          {detail.rejectionReason && <p className="text-sm text-gray-500 mt-1">Reason: {detail.rejectionReason}</p>}
        </Card>
      ) : !showRejectForm ? (
        <div className="flex gap-3">
          <button
            onClick={handleApprove}
            disabled={acting}
            className="flex-1 flex items-center justify-center gap-2 bg-eco-600 text-white rounded-md py-3 font-medium hover:bg-eco-700 disabled:opacity-50"
          >
            <CheckCircle2 size={18} /> Approve
          </button>
          <button
            onClick={() => setShowRejectForm(true)}
            disabled={acting}
            className="flex-1 flex items-center justify-center gap-2 border border-red-300 text-red-600 rounded-md py-3 font-medium hover:bg-red-50 disabled:opacity-50"
          >
            <XCircle size={18} /> Reject
          </button>
        </div>
      ) : (
        <Card>
          <form onSubmit={handleReject} className="space-y-3">
            <label className="block text-sm font-medium text-gray-600">Rejection reason (required)</label>
            <textarea
              required
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
              rows={3}
              className="w-full border border-gray-200 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-300"
              placeholder="e.g. Uploaded image does not provide sufficient evidence."
            />
            <div className="flex gap-3">
              <button type="submit" disabled={acting} className="flex-1 bg-red-600 text-white rounded-md py-2.5 font-medium hover:bg-red-700 disabled:opacity-50">
                Confirm Rejection
              </button>
              <button type="button" onClick={() => setShowRejectForm(false)} className="flex-1 border border-gray-200 rounded-md py-2.5 font-medium text-gray-600">
                Cancel
              </button>
            </div>
          </form>
        </Card>
      )}
    </div>
  )
}
