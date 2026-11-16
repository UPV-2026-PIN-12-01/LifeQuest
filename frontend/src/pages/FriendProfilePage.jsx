import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import ConfirmDialog from '../components/ConfirmDialog.jsx'
import { getFriendProfile, removeFriend } from '../services/friends.js'
import { activityById } from '../utils/taskOptions.js'
import { PlayerSummary } from './FriendsPage.jsx'
import { formatDuration } from '../utils/duration.js'
import '../styles/challenges.css'
import '../styles/friends.css'

const STATS = [
  { key: 'hp', label: 'Vida', icon: '❤️' },
  { key: 'strength', label: 'Fuerza', icon: '💪' },
  { key: 'speed', label: 'Velocidad', icon: '⚡' },
  { key: 'stamina', label: 'Estamina', icon: '🛡️' },
  { key: 'wisdom', label: 'Sabiduría', icon: '📖' },
]

function formatDay(date) {
  return new Date(`${date}T12:00:00`).toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'short' })
}

export default function FriendProfilePage() {
  const { friendUserId } = useParams()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [error, setError] = useState(null)
  const [confirmingRemove, setConfirmingRemove] = useState(false)
  const [removing, setRemoving] = useState(false)
  const [removeError, setRemoveError] = useState(null)

  useEffect(() => {
    let cancelled = false
    getFriendProfile(friendUserId)
      .then((result) => !cancelled && setProfile(result))
      .catch((err) => !cancelled && setError(err))
    return () => {
      cancelled = true
    }
  }, [friendUserId])

  async function remove() {
    setRemoving(true)
    setRemoveError(null)
    try {
      await removeFriend(friendUserId)
      navigate('/friends')
    } catch {
      setRemoveError('No se pudo eliminar al amigo. Inténtalo de nuevo.')
      setRemoving(false)
    }
  }

  if (error) {
    return (
      <main className="challenges-page">
        <Link to="/friends" className="friends-back">← Amigos</Link>
        <p className="challenges-error" role="alert">
          {error.status === 404 ? 'Este jugador no está en tu lista de amigos.' : 'No se pudo cargar el perfil.'}
        </p>
      </main>
    )
  }

  if (!profile) {
    return <main className="challenges-page"><p className="friends-muted">Cargando…</p></main>
  }

  const xpPercent = Math.min(100, Math.round((profile.xp / Math.max(1, profile.xpForNextLevel)) * 100))

  return (
    <main className="challenges-page">
      <Link to="/friends" className="friends-back">← Amigos</Link>

      <section className="friend-profile-card">
        <PlayerSummary player={profile.player} />
        <div className="friend-profile-streaks">
          <span>🔥 {profile.dayStreak} días de racha</span>
          <span>✅ {profile.taskStreak} tareas seguidas</span>
        </div>
        <div className="challenges-progress-heading">
          <span className="friends-muted">XP del nivel</span>
          <strong>{profile.xp} / {profile.xpForNextLevel}</strong>
        </div>
        <div className="challenges-progress-track" aria-hidden="true">
          <div className="challenges-progress-value" style={{ width: `${xpPercent}%` }} />
        </div>
        <ul className="friend-stats">
          {STATS.map((stat) => (
            <li key={stat.key}>
              <span aria-hidden="true">{stat.icon}</span>
              <span>{stat.label}</span>
              <strong>{profile[stat.key]}</strong>
            </li>
          ))}
        </ul>
      </section>

      <section className="friends-section" aria-labelledby="friend-tasks-title">
        <h2 id="friend-tasks-title">Tareas completadas esta semana</h2>
        {profile.completedTasks.length === 0 ? (
          <p className="friends-muted">No ha completado tareas en los últimos 7 días.</p>
        ) : (
          <ul className="friends-list">
            {profile.completedTasks.map((task) => (
              <li key={task.id} className="friend-row">
                <div className="friend-summary">
                  <span className="friend-task-icon" aria-hidden="true">{task.icon}</span>
                  <div>
                    <strong>{task.title}</strong>
                    <span className="friends-muted">{[activityById(task.activityType).label, formatDuration(task.durationMinutes), formatDay(task.date)]
                      .filter(Boolean).join(' · ')}</span>
                  </div>
                </div>
                <span className="challenge-status is-completed">+{task.xp} XP</span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <button type="button" className="friends-secondary-button friends-remove" onClick={() => setConfirmingRemove(true)}>
        Eliminar amigo
      </button>

      {confirmingRemove && (
        <ConfirmDialog
          title={`¿Eliminar a ${profile.player.playerName}?`}
          message="Dejaréis de ver vuestros personajes y la actividad del otro. Tendréis que enviar una nueva solicitud para volver a ser amigos."
          confirmLabel="Eliminar"
          busyLabel="Eliminando…"
          danger
          busy={removing}
          error={removeError}
          onConfirm={remove}
          onCancel={() => {
            setConfirmingRemove(false)
            setRemoveError(null)
          }}
        />
      )}
    </main>
  )
}
