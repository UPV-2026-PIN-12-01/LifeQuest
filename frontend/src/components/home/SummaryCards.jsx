export default function SummaryCards({ summary }) {
  const cards = [
    { id: 'challenges', icon: '🎯', value: summary.activeChallenges, label: 'Desafíos activos' },
    { id: 'training', icon: '🔥', value: summary.trainingInProgress, label: 'En progreso' },
    { id: 'rewards', icon: '🎁', value: `+${summary.weeklyXp}`, label: 'XP esta semana' },
  ]

  return (
    <ul className="summary-cards" aria-label="Resumen de la semana">
      {cards.map((card) => (
        <li key={card.id} className={`summary-card summary-${card.id}`}>
          <span className="summary-icon" aria-hidden="true">{card.icon}</span>
          <span className="summary-value">{card.value}</span>
          <span className="summary-label">{card.label}</span>
        </li>
      ))}
    </ul>
  )
}
