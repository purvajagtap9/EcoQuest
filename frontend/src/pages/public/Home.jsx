import { Link } from 'react-router-dom'
import { Leaf, Target, Coins, Award, Flame, BarChart3 } from 'lucide-react'

const FEATURES = [
  { icon: <Leaf size={22} />, title: 'Interactive Learning', text: 'Bite-sized EVS lessons on climate, waste, water, and more — with local Indian context.' },
  { icon: <Target size={22} />, title: 'Real Eco-Quests', text: 'Plant a tree, segregate waste, go plastic-free — real actions, not just quizzes.' },
  { icon: <Coins size={22} />, title: 'Eco-Coins', text: 'Earn coins for verified action, tracked in a transparent ledger.' },
  { icon: <Award size={22} />, title: 'Badges & Levels', text: 'Unlock achievements as you build real sustainable habits.' },
  { icon: <Flame size={22} />, title: 'Streaks', text: 'Stay consistent and build a daily eco-habit.' },
  { icon: <BarChart3 size={22} />, title: 'Real Impact', text: 'See your estimated trees planted, waste segregated, and more.' },
]

export default function Home() {
  return (
    <div>
      <section className="max-w-5xl mx-auto px-4 pt-20 pb-16 text-center">
        <div className="inline-flex items-center gap-2 text-eco-700 font-bold text-2xl mb-6">
          <Leaf size={32} /> EcoQuest India
        </div>
        <h1 className="text-4xl md:text-5xl font-bold text-gray-800 mb-4">
          Turn Environmental Learning Into <span className="text-eco-600">Real Action</span>
        </h1>
        <p className="text-gray-500 max-w-2xl mx-auto mb-8">
          A gamified platform where Indian students learn EVS, complete real-world sustainability
          quests, and see the measurable difference they're making.
        </p>
        <div className="flex items-center justify-center gap-3">
          <Link to="/register" className="bg-eco-600 text-white px-6 py-3 rounded-md font-medium hover:bg-eco-700">
            Get Started
          </Link>
          <Link to="/how-it-works" className="border border-eco-200 text-eco-700 px-6 py-3 rounded-md font-medium hover:bg-eco-50">
            How It Works
          </Link>
        </div>
      </section>

      <section className="max-w-6xl mx-auto px-4 pb-20 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        {FEATURES.map((f) => (
          <div key={f.title} className="bg-white rounded-xl border border-eco-100 p-5">
            <div className="text-eco-600 mb-3">{f.icon}</div>
            <h3 className="font-semibold text-gray-800 mb-1">{f.title}</h3>
            <p className="text-sm text-gray-500">{f.text}</p>
          </div>
        ))}
      </section>
    </div>
  )
}
