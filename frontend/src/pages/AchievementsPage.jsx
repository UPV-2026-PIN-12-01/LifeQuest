import { useEffect, useState } from 'react'
import { claimAchievement, getAchievements } from '../services/achievements.js'
import '../styles/achievements.css'

export default function AchievementsPage() {
  const [achievements, setAchievements] = useState([])
  const [selected, setSelected] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)
  const [claiming, setClaiming] = useState(false)
  const [rewardToast, setRewardToast] = useState(null)

  async function load() {
    setLoading(true)
    try {
      setAchievements(await getAchievements())
      setError(null)
    } catch (requestError) {
      setError(requestError)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  async function handleClaim() {
    if (!selected) return
    setClaiming(true)
    try {
      const claimed = await claimAchievement(selected.id)
      setRewardToast({ xp: claimed.xpReward, gold: claimed.goldReward })
      setSelected(null)
      await load()
    } catch (requestError) {
      setError(requestError)
    } finally {
      setClaiming(false)
    }
  }

  const unlocked = achievements.filter((achievement) => achievement.unlocked).length

  return (
    <main className="achievements-page">
      <header className="achievements-header">
        <div>
          <div className="achievements-title-row">
            <h1>Logros</h1>
            <span className="achievements-count">
              {loading ? 'Cargando...' : `${unlocked} / ${achievements.length}`}
            </span>
          </div>
          <p>Desbloquea logros completando tareas y objetivos.</p>
        </div>
      </header>

      {error && <div className="achievements-error" role="alert">No se pudieron cargar los logros. Inténtalo de nuevo.</div>}
      {loading && <p className="achievements-state">Cargando logros...</p>}
      {!loading && !error && achievements.length === 0 && (
        <p className="achievements-state">Todavía no hay logros disponibles.</p>
      )}
      {!loading && !error && (
        <section className="achievements-grid" aria-label="Lista de logros">
          {achievements.map((achievement) => (
            <button
              type="button"
              key={achievement.id}
              className={`achievement-card ${achievement.unlocked ? 'is-unlocked' : 'is-locked'}`}
              onClick={() => setSelected(achievement)}
            >
              <span className="achievement-icon" aria-hidden="true">{achievement.unlocked ? achievement.icon : '🔒'}</span>
              <span className="achievement-card-content">
                <strong>{achievement.name}</strong>
                <span>{achievement.description}</span>
                {achievement.unlocked
                  ? <span className="achievement-status">✓ Desbloqueado · {formatDate(achievement.unlockedAt)}</span>
                  : <span className="achievement-progress">{formatProgress(achievement)}</span>}
                <span className="achievement-reward">✨ +{achievement.xpReward} XP · 🪙 +{achievement.goldReward}</span>
              </span>
            </button>
          ))}
        </section>
      )}

      {selected && (
        <div className="achievement-modal-backdrop" role="presentation" onClick={() => setSelected(null)}>
          <section className="achievement-modal" role="dialog" aria-modal="true" aria-labelledby="achievement-title" onClick={(event) => event.stopPropagation()}>
            <button type="button" className="achievement-close" onClick={() => setSelected(null)} aria-label="Cerrar">×</button>
            <span className="achievement-modal-icon" aria-hidden="true">{selected.unlocked ? selected.icon : '🔒'}</span>
            <h2 id="achievement-title">{selected.name}</h2>
            <p>{selected.description}</p>
            <p><strong>Progreso:</strong> {formatProgress(selected)}</p>
            <p><strong>Recompensa:</strong> ✨ +{selected.xpReward} XP · 🪙 +{selected.goldReward}</p>
            {selected.unlocked && <p className="achievement-status">Desbloqueado el {formatDate(selected.unlockedAt)}</p>}
            {selected.unlocked && !selected.rewardClaimed && (
              <button type="button" className="achievement-claim-button" disabled={claiming} onClick={handleClaim}>
                {claiming ? 'Reclamando...' : 'Reclamar recompensa'}
              </button>
            )}
            {selected.unlocked && selected.rewardClaimed && <p className="achievement-claimed">✓ Recompensa reclamada</p>}
          </section>
        </div>
      )}

      {rewardToast && (
        <div className="achievement-reward-toast" role="status" onClick={() => setRewardToast(null)}>
          <strong>¡Recompensa conseguida!</strong>
          <span>✨ +{rewardToast.xp} XP · 🪙 +{rewardToast.gold}</span>
        </div>
      )}
    </main>
  )
}

function formatProgress(achievement) {
  if (achievement.unit === 'horas') {
    return `${(achievement.progress / 60).toLocaleString('es-ES', { maximumFractionDigits: 1 })}/${achievement.target / 60} h`
  }
  if (achievement.unit === 'nivel') return `nivel ${achievement.progress}/${achievement.target}`
  return `${achievement.progress}/${achievement.target} ${achievement.unit}`
}

function formatDate(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('es-ES').format(new Date(value))
}
