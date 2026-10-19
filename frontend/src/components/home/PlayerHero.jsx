import { classById } from '../../utils/playerClasses.js'

export default function PlayerHero({ player, weekStats }) {
  const playerClass = classById(player.playerClass)
  const xpPercent = Math.min(100, Math.round((player.xp / player.xpForNextLevel) * 100))

  const chips = [
    { id: 'streak', icon: '🔥', value: `${player.streakDays} días`, label: 'Racha' },
    { id: 'tasks', icon: '⚔️', value: `${weekStats.done} / ${weekStats.total}`, label: 'Tareas hechas' },
    { id: 'xp', icon: '💰', value: `+${weekStats.xp} XP`, label: 'Esta semana' },
  ]

  return (
    <section className="player-hero" aria-label="Tu personaje">
      <div className="player-avatar" style={{ '--xp-percent': `${xpPercent}%` }} aria-hidden="true">
        <span className="player-avatar-icon">{playerClass.icon}</span>
        <span className="player-avatar-level">Nv. {player.level}</span>
      </div>

      <div className="player-info">
        <p className="player-name">{player.username}</p>
        <p className="player-class">
          {playerClass.name} · {playerClass.stats}
        </p>
      </div>

      <div className="player-xp">
        <div className="player-xp-row">
          <span>
            Nivel {player.level} → {player.level + 1}
          </span>
          <span className="tabular">
            {player.xp} / {player.xpForNextLevel} XP
          </span>
        </div>
        <div
          className="xp-bar"
          role="progressbar"
          aria-label={`Nivel ${player.level}, experiencia hasta el siguiente nivel`}
          aria-valuemin={0}
          aria-valuemax={player.xpForNextLevel}
          aria-valuenow={player.xp}
        >
          <span className="xp-bar-fill" style={{ width: `${xpPercent}%` }} />
        </div>
      </div>

      <ul className="hero-chips" aria-label="Resumen de la semana">
        {chips.map((chip) => (
          <li key={chip.id} className={`hero-chip hero-chip-${chip.id}`}>
            <span className="hero-chip-icon" aria-hidden="true">{chip.icon}</span>
            <span className="hero-chip-text">
              <span className="hero-chip-value tabular">{chip.value}</span>
              <span className="hero-chip-label">{chip.label}</span>
            </span>
          </li>
        ))}
      </ul>
    </section>
  )
}
