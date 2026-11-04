import { useCallback, useEffect, useRef, useState } from 'react'
import { GOAL_REWARD_TIERS } from '../data.jsx'
import {
  claimGoal,
  createGoal,
  deleteGoal,
  evaluateGoal,
  getGoalQuota,
  getGoals,
} from '../services/goals.js'
import '../styles/challenges.css'

const WEEKLY_LIMIT = 3

export default function GoalsPage() {
  const [history, setHistory] = useState(false)
  const [activeGoals, setActiveGoals] = useState([])
  const [historyGoals, setHistoryGoals] = useState([])
  const [quota, setQuota] = useState({ createdThisWeek: 0, weeklyLimit: WEEKLY_LIMIT })
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [refreshVersion, setRefreshVersion] = useState(0)
  const [showForm, setShowForm] = useState(false)
  const [evaluation, setEvaluation] = useState(null)
  const [busyGoalId, setBusyGoalId] = useState(null)
  const [deletingGoalId, setDeletingGoalId] = useState(null)
  const [rewardToast, setRewardToast] = useState(null)
  const [now, setNow] = useState(0)
  const [formDefaults, setFormDefaults] = useState(null)

  const refresh = useCallback(() => setRefreshVersion((version) => version + 1), [])

  useEffect(() => {
    let cancelled = false
    Promise.all([getGoals(false), getGoals(true), getGoalQuota()])
      .then(([active, completed, currentQuota]) => {
        if (cancelled) return
        setActiveGoals(active)
        setHistoryGoals(completed)
        setQuota(currentQuota)
        setLoadError(null)
      })
      .catch((error) => {
        if (!cancelled) setLoadError(error)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [refreshVersion])

  useEffect(() => {
    const tick = () => setNow(new Date().getTime())
    const initial = window.setTimeout(tick, 0)
    const interval = window.setInterval(tick, 60_000)
    return () => {
      window.clearTimeout(initial)
      window.clearInterval(interval)
    }
  }, [])

  async function handleDelete(goal) {
    const location = history ? 'del historial' : 'de tus objetivos activos'
    if (!window.confirm(`¿Quieres eliminar este objetivo ${location}?`)) return
    setDeletingGoalId(goal.id)
    setLoadError(null)
    try {
      await deleteGoal(goal.id)
      refresh()
    } catch (error) {
      setLoadError(error)
    } finally {
      setDeletingGoalId(null)
    }
  }

  async function handleClaim(goal) {
    setBusyGoalId(goal.id)
    setLoadError(null)
    try {
      const claimed = await claimGoal(goal.id)
      setRewardToast({ xp: claimed.xpReward, gold: claimed.goldReward })
      refresh()
    } catch (error) {
      setLoadError(error)
    } finally {
      setBusyGoalId(null)
    }
  }

  async function handleEvaluate(goal, completed, rating) {
    setBusyGoalId(goal.id)
    setLoadError(null)
    try {
      await evaluateGoal(goal.id, { completed, rating: completed ? rating : null })
      setEvaluation(null)
      setHistory(true)
      refresh()
      return true
    } catch (error) {
      setLoadError(error)
      return false
    } finally {
      setBusyGoalId(null)
    }
  }

  const goals = history ? historyGoals : activeGoals
  const weeklyLimit = quota.weeklyLimit || WEEKLY_LIMIT
  const quotaReached = quota.createdThisWeek >= weeklyLimit

  return (
    <main className="challenges-page">
      <header className="challenges-header">
        <div>
          <h1>Objetivos</h1>
          <p>Define una meta, cumple el plazo y gana recompensas.</p>
        </div>
        <button
          type="button"
          className="challenges-primary-button"
          onClick={() => {
            setLoadError(null)
            const start = new Date(new Date().getTime() + 60_000)
            const end = new Date(start.getTime() + 13 * 60 * 60_000)
            setFormDefaults({
              minStart: localDateTimeInput(start),
              startDate: localDateTimeInput(start),
              endDate: localDateTimeInput(end),
            })
            setShowForm(true)
          }}
          disabled={quotaReached}
          title={quotaReached ? 'Ya has creado tres objetivos esta semana' : undefined}
        >
          {quotaReached ? 'Límite semanal alcanzado' : '+ Nuevo objetivo'}
        </button>
      </header>

      <section className="challenges-progress" aria-labelledby="goals-progress-title">
        <div className="challenges-progress-heading">
          <h2 id="goals-progress-title">Objetivos creados esta semana</h2>
          <strong>{quota.createdThisWeek} / {weeklyLimit}</strong>
        </div>
        <div className="challenges-progress-track" aria-hidden="true">
          <div
            className="challenges-progress-value"
            style={{ width: `${Math.min(100, (quota.createdThisWeek / weeklyLimit) * 100)}%` }}
          />
        </div>
      </section>

      <div className="challenges-tabs" role="tablist" aria-label="Objetivos">
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
          Objetivos <span>{activeGoals.length}</span>
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
          Historial <span>{historyGoals.length}</span>
        </button>
      </div>

      {loadError && (
        <p className="challenges-error" role="alert">
          {goalErrorMessage(loadError)}
        </p>
      )}

      {loading ? (
        <section className="challenges-empty-state" aria-live="polite">
          <p>Cargando objetivos…</p>
        </section>
      ) : goals.length > 0 ? (
        <section className="challenges-list" aria-live="polite">
          {goals.map((goal) => (
            <GoalCard
              key={goal.id}
              goal={goal}
              history={history}
              now={now}
              busy={busyGoalId === goal.id}
              deleting={deletingGoalId === goal.id}
              onEvaluate={() => setEvaluation(goal)}
              onClaim={() => handleClaim(goal)}
              onDelete={() => handleDelete(goal)}
            />
          ))}
        </section>
      ) : !loadError ? (
        <section className="challenges-empty-state" aria-live="polite">
          <h2>{history ? 'Todavía no tienes objetivos en el historial' : 'Todavía no tienes objetivos activos'}</h2>
          <p>{history ? 'Los objetivos completados o fallidos aparecerán aquí.' : 'Crea un objetivo para empezar.'}</p>
        </section>
      ) : null}

      {showForm && (
        <GoalFormModal
          quotaReached={quotaReached}
          defaults={formDefaults}
          onClose={() => setShowForm(false)}
          onCreated={() => {
            setShowForm(false)
            setHistory(false)
            refresh()
          }}
        />
      )}
      {evaluation && (
        <GoalEvaluationModal
          goal={evaluation}
          busy={busyGoalId === evaluation.id}
          onClose={() => setEvaluation(null)}
          onSubmit={(completed, rating) => handleEvaluate(evaluation, completed, rating)}
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

function GoalCard({ goal, history, now, busy, deleting, onEvaluate, onClaim, onDelete }) {
  const isSuccessful = goal.completed && goal.rating !== null
  const claimableAt = goal.rewardClaimableAt ? new Date(goal.rewardClaimableAt).getTime() : Infinity
  const canClaim = history && isSuccessful && !goal.rewardClaimed && now >= claimableAt
  const canEvaluate = !history && goal.endDate && now >= new Date(goal.endDate).getTime()
  const tier = GOAL_REWARD_TIERS.find((option) => option.level === goal.rewardTier)
  const hasReward = goal.xpReward > 0 || goal.goldReward > 0

  return (
    <article className="challenge-card">
      <button
        type="button"
        className="challenge-delete-button"
        onClick={onDelete}
        disabled={deleting}
        aria-label={`Eliminar objetivo ${history ? 'del historial' : 'activo'}`}
      >
        ×
      </button>
      <div className="challenge-card-heading">
        <div>
          <p className="challenge-task-name">
            {goal.rewardTier > 0
              ? `OBJETIVO · NIVEL ${goal.rewardTier}${tier ? ` · ${tier.name}` : ''}`
              : 'OBJETIVO · RECOMPENSA ANTERIOR'}
          </p>
          <h2>{goal.title}</h2>
        </div>
        <span className={`challenge-status${isSuccessful ? ' is-completed' : ''}`}>
          {goal.status === 'failed' ? 'No cumplido' : isSuccessful ? 'Completado' : 'Activo'}
        </span>
      </div>
      <p className="challenge-description">{goal.description}</p>
      <div className="challenge-card-meta">
        <span>Inicio: <strong>{formatDateTime(goal.startDate)}</strong></span>
        <span>Fin: <strong>{formatDateTime(goal.endDate)}</strong></span>
        <span>✨ {goal.xpReward} XP</span>
        <span>🪙 {goal.goldReward} oro</span>
      </div>
      <div className="challenge-card-actions">
        {history ? (
          isSuccessful ? (
            !hasReward ? (
              <span className="challenge-task-status">Sin recompensa configurada</span>
            ) : goal.rewardClaimed ? (
              <span className="challenge-task-status is-completed">Recompensa reclamada</span>
            ) : canClaim ? (
              <button type="button" className="challenge-action-button" onClick={onClaim} disabled={busy}>
                {busy ? 'Reclamando…' : '🎁 Reclamar recompensa'}
              </button>
            ) : (
              <span className="challenge-task-status">
                Disponible para reclamar: {formatDateTime(goal.rewardClaimableAt)}
              </span>
            )
          ) : (
            <span className="challenge-task-status is-not_completed">Este objetivo no se completó</span>
          )
        ) : (
          <button
            type="button"
            className="challenge-action-button"
            onClick={onEvaluate}
            disabled={!canEvaluate}
            title={!canEvaluate ? 'Podrás evaluar el objetivo cuando llegue su fecha de fin' : undefined}
          >
            {canEvaluate ? '✓ Evaluar objetivo' : `Evaluable el ${formatDateTime(goal.endDate)}`}
          </button>
        )}
      </div>
    </article>
  )
}

function GoalFormModal({ quotaReached, defaults, onClose, onCreated }) {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [startDate, setStartDate] = useState(defaults.startDate)
  const [endDate, setEndDate] = useState(defaults.endDate)
  const [rewardTier, setRewardTier] = useState(1)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const selectedTier = GOAL_REWARD_TIERS.find((tier) => tier.level === rewardTier)
  const minStart = defaults.minStart
  const minEnd = startDate
    ? localDateTimeInput(new Date(new Date(startDate).getTime() + 12 * 60 * 60_000))
    : minStart

  async function submit(event) {
    event.preventDefault()
    if (!title.trim() || !startDate || !endDate) {
      setError('Completa el título y las fechas del objetivo.')
      return
    }
    if (new Date(startDate) < new Date(minStart) || new Date(endDate) < new Date(minEnd)) {
      setError('El objetivo debe empezar ahora o más tarde y durar al menos 12 horas.')
      return
    }
    setBusy(true)
    setError(null)
    try {
      await createGoal({
        title: title.trim(),
        description: description.trim(),
        startDate,
        endDate,
        rewardTier,
      })
      onCreated()
    } catch (requestError) {
      setError(requestError.message || 'No se pudo crear el objetivo.')
      setBusy(false)
    }
  }

  const dialogRef = useRef(null)
  useEffect(() => {
    if (!dialogRef.current?.open) dialogRef.current?.showModal()
  }, [])

  return (
    <dialog
      ref={dialogRef}
      className="challenge-modal"
      onClose={onClose}
      onClick={(event) => {
        if (event.target === dialogRef.current) dialogRef.current.close()
      }}
    >
      <form className="challenge-form" onSubmit={submit} noValidate>
        <header className="challenge-form-header">
          <div>
            <h2>Crear objetivo</h2>
            <p>Máximo de tres objetivos por semana · duración mínima: 12 horas</p>
          </div>
          <button type="button" className="challenge-form-close" onClick={() => dialogRef.current.close()} aria-label="Cerrar">
            ×
          </button>
        </header>

        <label className="challenge-field">
          <span>Título</span>
          <input
            type="text"
            value={title}
            maxLength={120}
            onChange={(event) => setTitle(event.target.value)}
            placeholder="Ej. Completar el curso de React"
            required
          />
        </label>
        <label className="challenge-field">
          <span>Descripción</span>
          <textarea
            rows={3}
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Describe qué quieres conseguir..."
          />
        </label>
        <div className="challenge-custom-fields goal-date-fields">
          <label className="challenge-field">
            <span>Inicio</span>
            <input
              type="datetime-local"
              value={startDate}
              min={minStart}
              onChange={(event) => {
                const nextStart = event.target.value
                setStartDate(nextStart)
                if (nextStart && (!endDate || new Date(endDate) < new Date(new Date(nextStart).getTime() + 12 * 60 * 60_000))) {
                  setEndDate(localDateTimeInput(new Date(new Date(nextStart).getTime() + 12 * 60 * 60_000)))
                }
              }}
              required
            />
          </label>
          <label className="challenge-field">
            <span>Fin</span>
            <input
              type="datetime-local"
              value={endDate}
              min={minEnd}
              onChange={(event) => setEndDate(event.target.value)}
              required
            />
          </label>
        </div>
        <fieldset className="challenge-field">
          <legend>Nivel de recompensa</legend>
          <div className="challenge-difficulty-grid">
            {GOAL_REWARD_TIERS.map((tier) => (
              <label
                key={tier.level}
                className={`challenge-difficulty-choice${rewardTier === tier.level ? ' is-selected' : ''}`}
              >
                <input
                  type="radio"
                  name="goal-reward-tier"
                  value={tier.level}
                  checked={rewardTier === tier.level}
                  onChange={() => setRewardTier(tier.level)}
                />
                <span>
                  <strong>Nivel {tier.level}</strong>
                  <small>✨ {tier.xp} XP · 🪙 {tier.gold} oro</small>
                    <small>Reclama tras {tier.claimWaitHours} h desde el fin</small>
                </span>
              </label>
            ))}
          </div>
        </fieldset>
        {selectedTier && (
          <div className="challenge-reward-preview">
            <span>Recompensa seleccionada</span>
            <span>✨ {selectedTier.xp} XP · 🪙 {selectedTier.gold} oro</span>
          </div>
        )}
        {quotaReached && <p className="challenge-form-error" role="alert">Ya has creado tres objetivos esta semana.</p>}
        {error && <p className="challenge-form-error" role="alert">{error}</p>}

        <footer className="challenge-form-actions">
          <button type="button" className="challenge-form-cancel" onClick={() => dialogRef.current.close()} disabled={busy}>
            Cancelar
          </button>
          <button type="submit" className="challenge-form-submit" disabled={busy || quotaReached}>
            {busy ? 'Creando…' : 'Crear objetivo'}
          </button>
        </footer>
      </form>
    </dialog>
  )
}

function GoalEvaluationModal({ goal, busy, onClose, onSubmit }) {
  const [rating, setRating] = useState(5)
  const [error, setError] = useState(null)

  async function submit(completed) {
    setError(null)
    const saved = await onSubmit(completed, completed ? rating : null)
    if (!saved) setError('No se pudo guardar la evaluación. Inténtalo de nuevo.')
  }

  return (
    <dialog className="challenge-modal challenge-evaluation-modal" open>
      <div className="challenge-evaluation">
        <h2>Evaluar objetivo</h2>
        <p>“{goal.title}”</p>
        <h3>¿Has cumplido el objetivo?</h3>
        <div className="challenge-evaluation-actions">
          <button
            type="button"
            className="challenge-success-button"
            onClick={() => submit(true)}
            disabled={busy}
          >
            {busy ? 'Guardando…' : '✓ Sí, lo cumplí'}
          </button>
          <button
            type="button"
            className="challenge-failure-button"
            onClick={() => submit(false)}
            disabled={busy}
          >
            × No lo cumplí
          </button>
        </div>
        <label className="challenge-rating">
          Satisfacción: {rating}/10
          <input
            type="range"
            min="1"
            max="10"
            value={rating}
            onChange={(event) => setRating(Number(event.target.value))}
          />
        </label>
        {error && <p className="challenge-form-error" role="alert">{error}</p>}
        <button type="button" className="challenge-evaluation-cancel" onClick={onClose} disabled={busy}>
          Cancelar
        </button>
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
        <strong>¡Objetivo reclamado!</strong>
        <p>La recompensa se ha añadido a tu personaje.</p>
        <div className="reward-toast-values">
          <span>✨ +{reward.xp} XP</span>
          <span>🪙 +{reward.gold} oro</span>
        </div>
      </div>
      <button type="button" onClick={onClose} aria-label="Cerrar">×</button>
    </div>
  )
}

function goalErrorMessage(error) {
  if (error.status === 401) return 'Tu sesión ha caducado. Vuelve a iniciar sesión.'
  if (error.status === 404) return 'El backend todavía no tiene disponible la función de objetivos (404).'
  if (error.status === 409) return error.message
  if (error.status >= 500) return `El backend no pudo completar la operación (${error.status}). ${error.message || ''}`
  return `No se pudieron cargar o actualizar los objetivos${error.status ? ` (${error.status})` : ''}.`
}

function localDateTimeInput(date) {
  const offset = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

function formatDateTime(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('es-ES', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}
