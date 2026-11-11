import { useCallback, useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import AvatarDisplay from '../components/AvatarDisplay.jsx'
import {
  acceptFriendRequest,
  deleteFriendRequest,
  getFriendFeed,
  getFriends,
  sendFriendRequest,
} from '../services/friends.js'
import '../styles/challenges.css'
import '../styles/friends.css'
import { activityById } from '../utils/taskOptions.js'
import { formatDuration } from '../utils/duration.js'

const SEND_ERRORS = {
  404: 'No existe ningún jugador con ese nombre.',
  400: 'No puedes añadirte a ti mismo.',
  409: 'Ya sois amigos o la solicitud ya está enviada.',
}

export default function FriendsPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const tab = searchParams.get('tab') === 'activity' ? 'activity' : 'friends'
  const [data, setData] = useState({ friends: [], incoming: [], outgoing: [] })
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [playerName, setPlayerName] = useState('')
  const [sendState, setSendState] = useState({ busy: false, message: null, error: false })
  const [busyRequestId, setBusyRequestId] = useState(null)

  const load = useCallback(() => {
    return getFriends()
      .then((overview) => {
        setData(overview)
        setLoadError(null)
      })
      .catch(setLoadError)
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    load()
  }, [load])

  async function submit(event) {
    event.preventDefault()
    const name = playerName.trim()
    if (!name) return
    setSendState({ busy: true, message: null, error: false })
    try {
      const request = await sendFriendRequest(name)
      setPlayerName('')
      setSendState({
        busy: false,
        error: false,
        message: request?.incoming === false && request?.player
          ? `Solicitud enviada a ${request.player.playerName}.`
          : 'Solicitud enviada.',
      })
      await load()
    } catch (error) {
      setSendState({
        busy: false,
        error: true,
        message: SEND_ERRORS[error.status] ?? 'No se pudo enviar la solicitud.',
      })
    }
  }

  async function answer(requestId, accept) {
    setBusyRequestId(requestId)
    try {
      await (accept ? acceptFriendRequest(requestId) : deleteFriendRequest(requestId))
      await load()
    } catch (error) {
      setLoadError(error)
    } finally {
      setBusyRequestId(null)
    }
  }

  return (
    <main className="challenges-page">
      <header className="challenges-header">
        <div>
          <h1>Amigos</h1>
          <p>Añade amigos para ver sus personajes y las tareas que van completando.</p>
        </div>
      </header>

      <div className="challenges-tabs" role="tablist" aria-label="Amigos">
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'friends'}
          className={`challenges-tab${tab === 'friends' ? ' is-active' : ''}`}
          onClick={() => setSearchParams({}, { replace: true })}
        >
          Amigos <span>{data.friends.length}</span>
          {data.incoming.length > 0 && <span className="friends-badge" title="Solicitudes pendientes">{data.incoming.length}</span>}
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={tab === 'activity'}
          className={`challenges-tab${tab === 'activity' ? ' is-active' : ''}`}
          onClick={() => setSearchParams({ tab: 'activity' }, { replace: true })}
        >
          Actividad
        </button>
      </div>

      {tab === 'activity' ? <ActivityFeed hasFriends={data.friends.length > 0} /> : (
      <>
      <form className="friends-add" onSubmit={submit} noValidate>
        <label htmlFor="friend-player-name">Añadir amigo por nombre de jugador</label>
        <div className="friends-add-row">
          <input
            id="friend-player-name"
            value={playerName}
            onChange={(event) => setPlayerName(event.target.value)}
            placeholder="Nombre del personaje"
            maxLength={80}
            autoComplete="off"
          />
          <button type="submit" className="challenges-primary-button" disabled={sendState.busy || !playerName.trim()}>
            {sendState.busy ? 'Enviando…' : 'Enviar solicitud'}
          </button>
        </div>
        {sendState.message && (
          <p className={sendState.error ? 'challenges-error' : 'friends-success'} role={sendState.error ? 'alert' : 'status'}>
            {sendState.message}
          </p>
        )}
      </form>

      {loadError && (
        <p className="challenges-error" role="alert">No se pudieron cargar tus amigos. Inténtalo de nuevo.</p>
      )}

      {data.incoming.length > 0 && (
        <section className="friends-section" aria-labelledby="friends-incoming-title">
          <h2 id="friends-incoming-title">Solicitudes recibidas</h2>
          <ul className="friends-list">
            {data.incoming.map((request) => (
              <li key={request.id} className="friend-row">
                <PlayerSummary player={request.player} />
                <div className="friend-row-actions">
                  <button type="button" className="challenge-action-button" disabled={busyRequestId === request.id} onClick={() => answer(request.id, true)}>
                    Aceptar
                  </button>
                  <button type="button" className="friends-secondary-button" disabled={busyRequestId === request.id} onClick={() => answer(request.id, false)}>
                    Rechazar
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="friends-section" aria-labelledby="friends-list-title">
        <h2 id="friends-list-title">Tus amigos {data.friends.length > 0 && <span>({data.friends.length})</span>}</h2>
        {loading ? (
          <p className="friends-muted">Cargando…</p>
        ) : data.friends.length === 0 ? (
          <div className="challenges-empty-state">
            <h2>Aún no tienes amigos</h2>
            <p>Escribe el nombre del personaje de un compañero para enviarle una solicitud.</p>
          </div>
        ) : (
          <ul className="friends-list">
            {data.friends.map((friend) => (
              <li key={friend.userId}>
                <Link to={`/friends/${friend.userId}`} className="friend-row is-link">
                  <PlayerSummary player={friend} />
                  <span className="friends-muted" aria-hidden="true">Ver personaje →</span>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </section>

      {data.outgoing.length > 0 && (
        <section className="friends-section" aria-labelledby="friends-outgoing-title">
          <h2 id="friends-outgoing-title">Solicitudes enviadas</h2>
          <ul className="friends-list">
            {data.outgoing.map((request) => (
              <li key={request.id} className="friend-row">
                <PlayerSummary player={request.player} />
                <button type="button" className="friends-secondary-button" disabled={busyRequestId === request.id} onClick={() => answer(request.id, false)}>
                  Cancelar
                </button>
              </li>
            ))}
          </ul>
        </section>
      )}
      </>
      )}
    </main>
  )
}

function dayLabel(date) {
  const today = new Date()
  const iso = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  if (date === iso(today)) return 'Hoy'
  const yesterday = new Date(today)
  yesterday.setDate(today.getDate() - 1)
  if (date === iso(yesterday)) return 'Ayer'
  const label = new Date(`${date}T12:00:00`).toLocaleDateString('es-ES', { weekday: 'long', day: 'numeric', month: 'long' })
  return label.charAt(0).toUpperCase() + label.slice(1)
}

// Completed tasks of every friend in the last 7 days, grouped by day, newest first (the server sorts them).
function ActivityFeed({ hasFriends }) {
  const [entries, setEntries] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false
    getFriendFeed()
      .then((feed) => !cancelled && setEntries(feed))
      .catch((err) => !cancelled && setError(err))
    return () => {
      cancelled = true
    }
  }, [])

  if (error) return <p className="challenges-error" role="alert">No se pudo cargar la actividad.</p>
  if (entries === null) return <p className="friends-section friends-muted">Cargando…</p>
  if (entries.length === 0) {
    return (
      <div className="challenges-empty-state">
        <h2>Sin actividad esta semana</h2>
        <p>{hasFriends
          ? 'Cuando tus amigos completen tareas aparecerán aquí.'
          : 'Añade amigos desde la pestaña Amigos para ver lo que van completando.'}</p>
      </div>
    )
  }

  const days = []
  for (const entry of entries) {
    const last = days.at(-1)
    if (last?.date === entry.task.date) last.entries.push(entry)
    else days.push({ date: entry.task.date, entries: [entry] })
  }

  return days.map((day) => (
    <section key={day.date} className="friends-section" aria-label={dayLabel(day.date)}>
      <h2>{dayLabel(day.date)}</h2>
      <ul className="friends-list">
        {day.entries.map(({ player, task }) => (
          <li key={`${player.userId}-${task.id}`}>
            <Link to={`/friends/${player.userId}`} className="friend-row is-link">
              <div className="friend-summary">
                <AvatarDisplay avatar={player.profileUrl || player.userIcon || '⚔️'} size={44} />
                <div>
                  <strong>{task.icon} {task.title}</strong>
                  <span className="friends-muted">
                    {[player.playerName, activityById(task.activityType).label, formatDuration(task.durationMinutes)]
                      .filter(Boolean).join(' · ')}
                  </span>
                </div>
              </div>
              <span className="challenge-status is-completed">+{task.xp} XP</span>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  ))
}

export function PlayerSummary({ player }) {
  return (
    <div className="friend-summary">
      <AvatarDisplay avatar={player.profileUrl || player.userIcon || '⚔️'} size={44} />
      <div>
        <strong>{player.playerName}</strong>
        <span className="friends-muted">{player.playerClass} · Nivel {player.level}</span>
      </div>
    </div>
  )
}
