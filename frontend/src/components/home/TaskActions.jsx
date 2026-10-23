import { useEffect, useState } from 'react'
import { completeTask, skipTask, startTask } from '../../services/tasks.js'

// Mirrors the backend rules so the right buttons show; the server still checks every one.
const EARLY_CHECKIN_MS = 5 * 60_000

const VERIFICATION_LABELS = {
  focus: 'Verificada con foco (×1,5 XP)',
  checkin: 'Verificada con check-in (×1,2 XP)',
  none: 'Confirmada por ti',
}

// Check-in, check-out and the final answer of a task. Calls onChanged with the task the server returns.
export default function TaskActions({ task, onChanged }) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)
  const now = useNow(task.sessionOpen ? 1000 : 30_000)

  const start = new Date(`${task.date}T${task.startTime}`)
  const canCheckIn = now >= start.getTime() - EARLY_CHECKIN_MS
  const isOpenTask = task.status === 'pending' || task.status === 'in_progress' || task.status === 'finished'

  async function run(action) {
    setBusy(true)
    setError(null)
    try {
      onChanged(await action(task.id))
    } catch (requestError) {
      setError(actionErrorMessage(requestError))
    } finally {
      setBusy(false)
    }
  }

  if (task.status === 'completed') {
    return (
      <p className="task-verification">
        {VERIFICATION_LABELS[task.verification] ?? VERIFICATION_LABELS.none} · +{task.xp} XP
      </p>
    )
  }
  if (!isOpenTask) return null

  return (
    <div className="task-actions">
      {task.sessionOpen && (
        <div className="task-session" role="status">
          <strong className="tabular">⏱ {formatDuration(now - Date.parse(task.sessionStartedAt))}</strong>
          <span>Foco registrado: {Math.floor(task.focusedSeconds / 60)} min</span>
          <small>
            Con esta página abierta ganas el bonus de foco (×1,5). Si sales a entrenar o con amigos, pulsa Terminar al
            acabar y cuenta como check-in (×1,2).
          </small>
        </div>
      )}

      {!task.sessionStartedAt && task.status !== 'finished' && (
        <button
          className="task-modal-action"
          type="button"
          disabled={busy || !canCheckIn}
          onClick={() => run(startTask)}
        >
          {canCheckIn ? 'Empezar' : `Podrás empezar a las ${task.startTime}`}
        </button>
      )}

      {(task.sessionOpen || task.status === 'finished') && (
        <div className="task-actions-row">
          <button className="task-modal-action" type="button" disabled={busy} onClick={() => run(completeTask)}>
            {task.sessionOpen ? 'Terminar y completar' : 'Sí, la completé'}
          </button>
          <button
            className="task-modal-action task-modal-action-secondary"
            type="button"
            disabled={busy}
            onClick={() => run(skipTask)}
          >
            No la hice
          </button>
        </div>
      )}

      {error && (
        <p className="task-actions-error" role="alert">
          {error}
        </p>
      )}
    </div>
  )
}

function useNow(intervalMs) {
  const [now, setNow] = useState(() => Date.now())
  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), intervalMs)
    return () => clearInterval(timer)
  }, [intervalMs])
  return now
}

function formatDuration(ms) {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000))
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = String(Math.floor((totalSeconds % 3600) / 60)).padStart(2, '0')
  const seconds = String(totalSeconds % 60).padStart(2, '0')
  return hours > 0 ? `${hours}:${minutes}:${seconds}` : `${minutes}:${seconds}`
}

function actionErrorMessage(error) {
  if (error.status === 0) return 'No hay conexión. Inténtalo de nuevo.'
  if (error.message === 'Task can be completed only after it ends') {
    return 'Aún no ha terminado. Empieza la tarea para poder completarla antes.'
  }
  if (error.message === 'Task can no longer be confirmed') return 'Han pasado más de 24 h; ya no se puede confirmar.'
  if (error.message === 'Task was already answered') return 'Esta tarea ya estaba respondida.'
  if (error.message === 'Task has not started yet') return 'Todavía no puedes empezar esta tarea.'
  if (error.message === 'Task has already ended') return 'La tarea ya ha terminado; confírmala sin empezarla.'
  return 'No se pudo guardar. Inténtalo de nuevo.'
}
