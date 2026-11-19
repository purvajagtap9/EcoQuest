export default function Spinner({ size = 24 }) {
  return (
    <div
      className="animate-spin rounded-full border-2 border-eco-200 border-t-eco-600"
      style={{ width: size, height: size }}
    />
  )
}
