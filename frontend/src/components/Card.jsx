export default function Card({ children, className = '' }) {
  return (
    <div className={`bg-white rounded-xl shadow-sm border border-eco-100 p-5 ${className}`}>
      {children}
    </div>
  )
}
