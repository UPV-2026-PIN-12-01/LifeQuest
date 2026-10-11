import { classById } from '../../utils/playerClasses.js'
import SummaryCards from './SummaryCards.jsx'

export default function PlayerHero({ player, summary }) {
  const playerClass = classById(player.playerClass)
  const xpPercent = Math.min(100, Math.round((player.xp / player.xpForNextLevel) * 100))

  return (
    <section className="player-hero" aria-label="Tu personaje">
      <div className="player-identity">
        <span className={`player-avatar is-${playerClass.id.toLowerCase()}`} aria-hidden="true">
          {playerClass.icon}
        </span>
        <div className="player-info">
          <p className="player-name">{player.username}</p>
          <p className="player-class">{playerClass.name}</p>
          <div className="player-level">
            <span className="level-badge">Nv. {player.level}</span>
            <div
              className="xp-bar"
              role="progressbar"
              aria-label="Experiencia hasta el siguiente nivel"
              aria-valuemin={0}
              aria-valuemax={player.xpForNextLevel}
              aria-valuenow={player.xp}
            >
              <span className="xp-bar-fill" style={{ width: `${xpPercent}%` }} />
            </div>
            <span className="xp-text">
              {player.xp} / {player.xpForNextLevel} XP
            </span>
          </div>
        </div>
      </div>
      <SummaryCards summary={summary} />
    </section>
  )
}
