const STEPS = [
  { title: 'Learn', text: 'Work through short EVS lessons on topics like waste management and water conservation.' },
  { title: 'Quiz', text: 'Test your knowledge and earn your first Eco-Coins and XP.' },
  { title: 'Take on a Quest', text: 'Pick a real-world challenge — plant a tree, segregate waste, go plastic-free for a day.' },
  { title: 'Submit Proof', text: 'Upload a photo and, where needed, your location as evidence.' },
  { title: 'AI + Teacher Review', text: 'AI gives an initial read; your teacher makes the final call and approves your reward.' },
  { title: 'Earn & Track Impact', text: 'Collect Eco-Coins, XP, and badges — and watch your real environmental impact add up.' },
]

export default function HowItWorks() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-16">
      <h1 className="text-3xl font-bold text-gray-800 mb-8">How It Works</h1>
      <ol className="space-y-6">
        {STEPS.map((step, i) => (
          <li key={step.title} className="flex gap-4">
            <div className="flex-shrink-0 w-8 h-8 rounded-full bg-eco-100 text-eco-700 font-bold flex items-center justify-center">
              {i + 1}
            </div>
            <div>
              <h3 className="font-semibold text-gray-800">{step.title}</h3>
              <p className="text-sm text-gray-500">{step.text}</p>
            </div>
          </li>
        ))}
      </ol>
    </div>
  )
}
