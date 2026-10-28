import { useEffect, useState } from 'react'
import ChallengeFormModal from '../components/challenges/ChallengeFormModal.jsx'
import { claimChallenge, evaluateChallenge, getChallenges } from '../services/challenges.js'
import { getTasksForWeek } from '../services/tasks.js'
import '../styles/challenges.css'

export default function ChallengesPage() {
  const [history, setHistory] = useState(false)
  const [challenges, setChallenges] = useState([])
  const [activeCount, setActiveCount] = useState(0)
  const [historyCount, setHistoryCount] = useState(0)
  const [loadError, setLoadError] = useState(null)
  const [tasks, setTasks] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [tasksLoading, setTasksLoading] = useState(false)
  const [evaluation, setEvaluation] = useState(null)
  const [busyChallengeId, setBusyChallengeId] = useState(null)
  const [refreshVersion, setRefreshVersion] = useState(0)
  const [rewardToast, setRewardToast] = useState(null)

  useEffect(() => {
    let cancelled = false
    getChallenges(history).then((loadedChallenges) => {
      if (cancelled) return
      setChallenges(loadedChallenges)
      if (history) setHistoryCount(loadedChallenges.length)
      else setActiveCount(loadedChallenges.length)
    }).catch((error) => {
      if (!cancelled) setLoadError(error)
    })
    return () => {
      cancelled = true
    }
  }, [history, refreshVersion])

  const todayCount = challenges.filter((challenge) => challenge.task?.date === today()).length

  async function openCreateForm() {
    setLoadError(null)
    setShowForm(true)
    setTasksLoading(true)
    try {
      setTasks(await getTasksForWeek(new Date()))
    } catch (error) {
      setLoadError(error)
    } finally {
      setTasksLoading(false)
    }
  }

  async function handleClaim(challenge) {
    setBusyChallengeId(challenge.id)
    setLoadError(null)
    try {
      const claimedChallenge = await claimChallenge(challenge.id)
      setRewardToast({ xp: claimedChallenge.xp, gold: claimedChallenge.gold })
      setRefreshVersion((version) => version + 1)
    } catch (error) {
      setLoadError(error)
    } finally {
      setBusyChallengeId(null)
    }
  }

  return (
    <main className="challenges-page">
      <header className="challenges-header">
        <div>
          <h1>Desafíos</h1>
          <p>Convierte tus tareas en retos más exigentes.</p>
        </div>
        <button type="button" className="challenges-primary-button" onClick={openCreateForm}>
          + Nuevo desafío
        </button>
      </header>

      <section className="challenges-progress" aria-labelledby="challenges-progress-title">
        <div className="challenges-progress-heading">
          <h2 id="challenges-progress-title">Desafíos de hoy</h2>
          <strong>{todayCount} / 3</strong>
        </div>
        <div className="challenges-progress-track" aria-hidden="true">
          <div
            className="challenges-progress-value"
            style={{ width: `${Math.min(100, (todayCount / 3) * 100)}%` }}
          />
        </div>
      </section>

      <div className="challenges-tabs" role="tablist" aria-label="Desafíos">
        <button
          type="button"
          role="tab"
          aria-selected={!history}
          className={`challenges-tab${history ? '' : ' is-active'}`}
          onClick={() => {
            setLoadError(null)
            setHistory(false)
          }}
        >
          Activos <span>{activeCount}</span>
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={history}
          className={`challenges-tab${history ? ' is-active' : ''}`}
          onClick={() => {
            setLoadError(null)
            setHistory(true)
          }}
        >
          Historial           <span>{historyCount}</span>
        </button>
      </div>

      {loadError && (
        <p className="challenges-error" role="alert">
          {challengeLoadErrorMessage(loadError)}
        </p>
      )}

      {challenges.length > 0 ? (
        <section className="challenges-list" aria-live="polite">
          {challenges.map((challenge) => (
            <ChallengeCard
              key={challenge.id}
              challenge={challenge}
              history={history}
              busy={busyChallengeId === challenge.id}
              onEvaluate={() => setEvaluation(challenge)}
              onClaim={() => handleClaim(challenge)}
            />
          ))}
        </section>
      ) : !loadError ? (
        <section className="challenges-empty-state" aria-live="polite">
          <h2>{history ? 'Todavía no tienes desafíos completados' : 'Todavía no tienes desafíos'}</h2>
          <p>Convierte una tarea programada en un desafío para empezar.</p>
        </section>
      ) : null}
      {showForm && (
        <ChallengeFormModal
          tasks={tasks}
          loading={tasksLoading}
          onClose={() => setShowForm(false)}
          onCreated={() => {
            setShowForm(false)
            setHistory(false)
            setLoadError(null)
            setRefreshVersion((version) => version + 1)
          }}
        />
      )}
      {evaluation && (
        <ChallengeEvaluationModal
          challenge={evaluation}
          onClose={() => setEvaluation(null)}
          onSaved={() => {
            setEvaluation(null)
            setRefreshVersion((version) => version + 1)
          }}
        />
      )}
      {rewardToast && (
        <RewardToast
          reward={rewardToast}
          onClose={() => setRewardToast(null)}
        />
      )}
    </main>
  )
}

function challengeLoadErrorMessage(error) {
  if (error.status === 401) return 'Tu sesión ha caducado. Vuelve a iniciar sesión.'
  if (error.status === 404) return 'El backend desplegado todavía no tiene disponible la función de desafíos (404).'
  if (error.status >= 500) {
    return `El backend no pudo cargar los desafíos (${error.status}).${error.message ? ` ${error.message}` : ''}`
  }
  return `No se pudieron cargar tus desafíos${error.status ? ` (${error.status})` : ''}. Inténtalo de nuevo más tarde.`
}

function ChallengeCard({ challenge, history, busy, onEvaluate, onClaim }) {
  const task = challenge.task
  const difficulty = difficultyLabel(challenge.difficulty)

  return (
    <article className="challenge-card">
      <div className="challenge-card-heading">
        <div>
          <p className="challenge-task-name">
            {task?.icon || '🎯'} {task?.title || 'Tarea programada'}
          </p>
          <h2>{challenge.name}</h2>
        </div>
        <span className={`challenge-status${challenge.completed ? ' is-completed' : ''}`}>
          {challenge.completed ? 'Desafío superado' : 'Activo'}
        </span>
      </div>
      <p className="challenge-description">
        {challenge.requirement || challenge.description || 'Completa la condición del desafío.'}
      </p>
      <div className="challenge-card-meta">
        <span className={`challenge-difficulty difficulty-${difficulty.className}`}>
          {difficulty.icon} {difficulty.label}
        </span>
        <span>+{challenge.xp} XP · +{challenge.gold} 🪙</span>
        {challenge.rating && <strong>Autorealización: {challenge.rating}/10</strong>}
      </div>
      <div className="challenge-card-actions">
        <span className={`challenge-task-status is-${task?.status || 'unknown'}`}>
          Tarea: {taskStatusLabel(task?.status)}
        </span>
        {history ? (
          !challenge.rewardClaimed && (
            <button type="button" className="challenge-action-button" onClick={onClaim} disabled={busy}>
              {busy ? 'Reclamando…' : '⚔️ Reclamar desafío'}
            </button>
          )
        ) : (
          <button type="button" className="challenge-action-button" onClick={onEvaluate}>
            ⚔️ He cumplido el desafío
          </button>
        )}
      </div>
    </article>
  )
}

function ChallengeEvaluationModal({ challenge, onClose, onSaved }) {
  const [rating, setRating] = useState('')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  async function submit(completed) {
    if (completed && !rating) {
      setError('Selecciona una valoración del 1 al 10.')
      return
    }

    setBusy(true)
    setError(null)
    try {
      await evaluateChallenge(challenge.id, {
        completed,
        rating: completed ? Number(rating) : null,
      })
      onSaved()
    } catch (requestError) {
      setError(requestError.message || 'No se pudo evaluar el desafío.')
      setBusy(false)
    }
  }

  return (
    <dialog className="challenge-modal challenge-evaluation-modal" open>
      <div className="challenge-evaluation">
        <h2>Evaluar desafío</h2>
        <p>“{challenge.name}”</p>
        <h3>¿Has cumplido la condición?</h3>
        <div className="challenge-evaluation-actions">
          <button type="button" className="challenge-success-button" onClick={() => submit(true)} disabled={busy}>
            ✓ Sí, lo superé
          </button>
          <button type="button" className="challenge-failure-button" onClick={() => submit(false)} disabled={busy}>
            × No lo cumplí
          </button>
        </div>
        <label className="challenge-rating">
          Autovaloración: {rating || '—'}/10
          <input type="range" min="1" max="10" value={rating || 5} onChange={(event) => setRating(event.target.value)} />
        </label>
        {error && <p className="challenge-form-error" role="alert">{error}</p>}
        <button type="button" className="challenge-evaluation-cancel" onClick={onClose} disabled={busy}>Cancelar</button>
      </div>
    </dialog>
  )
}

function RewardToast({ reward, onClose }) {
  useEffect(() => {
    const timer = window.setTimeout(onClose, 5000)
    return () => window.clearTimeout(timer)
  }, [onClose])

  return (
    <div className="reward-toast" role="status">
      <div className="reward-toast-icon" aria-hidden="true">🎉</div>
      <div>
        <strong>¡Desafío reclamado!</strong>
        <p>Tu recompensa se ha añadido a tu personaje.</p>
        <div className="reward-toast-values">
          <span>✨ +{reward.xp} XP</span>
          <span>🪙 +{reward.gold}</span>
        </div>
      </div>
      <button type="button" onClick={onClose} aria-label="Cerrar">×</button>
    </div>
  )
}

function difficultyLabel(difficulty) {
  const labels = {
    EASY: { label: 'Fácil', className: 'easy', icon: '🟢' },
    NORMAL: { label: 'Normal', className: 'normal', icon: '🟡' },
    HARD: { label: 'Difícil', className: 'hard', icon: '🟠' },
    'VERY HARD': { label: 'Muy difícil', className: 'very-hard', icon: '🔴' },
    'LEGENDARY FEAT': { label: 'Legendario', className: 'legendary', icon: '🟣' },
  }
  return labels[difficulty] || labels.NORMAL
}

function today() {
  return new Date().toISOString().slice(0, 10)
}

function taskStatusLabel(status) {
  return {
    completed: 'Completada',
    finished: 'Finalizada',
    in_progress: 'En progreso',
    pending: 'Pendiente',
    not_completed: 'No completada',
  }[status] || 'No disponible'
}
