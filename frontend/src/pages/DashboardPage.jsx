import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useOutletContext } from 'react-router-dom'
import {
  getCachedTasksForWeek,
  getTasksForWeek,
  setCachedTasksForWeek,
} from '../services/tasks.js'
import { addDays, startOfWeek, weekDays, weekRangeLabel } from '../utils/week.js'
import '../styles/dashboard.css'

const EMPTY_TASKS = []

export default function DashboardPage() {
  const navigate = useNavigate()
  const { player } = useOutletContext()
  const [weekOffset, setWeekOffset] = useState(0)
  const [taskData, setTaskData] = useState(() => {
    const initialWeek = startOfWeek(new Date())
    return {
      weekKey: initialWeek.getTime(),
      tasks: getCachedTasksForWeek(initialWeek) ?? EMPTY_TASKS,
    }
  })
  const [loadError, setLoadError] = useState(null)

  const weekStart = addDays(startOfWeek(new Date()), weekOffset * 7)
  const weekKey = weekStart.getTime()
  const cachedTasks = getCachedTasksForWeek(weekStart)
  const tasks = taskData.weekKey === weekKey ? taskData.tasks : cachedTasks ?? EMPTY_TASKS

  useEffect(() => {
    let cancelled = false
    const requestedWeekStart = new Date(weekKey)

    getTasksForWeek(requestedWeekStart).then((weekTasks) => {
      if (cancelled) return
      setTaskData({ weekKey, tasks: weekTasks })
      setCachedTasksForWeek(requestedWeekStart, weekTasks)
      setLoadError(null)
    }).catch((error) => {
      if (!cancelled) setLoadError(error)
    })

    return () => {
      cancelled = true
    }
  }, [weekKey])

  useEffect(() => {
    if (loadError?.status === 401) {
      navigate('/login', { replace: true })
    }
  }, [loadError, navigate])

  const summary = useMemo(() => {
    const completed = tasks.filter((task) => task.status === 'completed')
    const notDone = tasks.filter((task) => task.status === 'not_completed')
    const pending = tasks.filter((task) => !['completed', 'not_completed'].includes(task.status))
    const resolved = completed.length + notDone.length
    const percentage = resolved === 0 ? null : Math.round((completed.length / resolved) * 100)
    const xpByDay = weekDays(weekStart).map((day) => ({
      ...day,
      xp: completed
        .filter((task) => task.date === day.iso)
        .reduce((total, task) => total + Number(task.xp || 0), 0),
    }))
    const goldByDay = weekDays(weekStart).map((day) => ({
      ...day,
      gold: completed
        .filter((task) => task.date === day.iso)
        .reduce((total, task) => total + Number(task.gold || 0), 0),
    }))

    return {
      completed: completed.length,
      notDone: notDone.length,
      pending: pending.length,
      percentage,
      xpByDay,
      goldByDay,
    }
  }, [tasks, weekStart])

  const hasTasks = tasks.length > 0
  const level = Number(player.level) || 1
  const xp = Number(player.xp) || 0
  const xpGoal = Number(player.xpForNextLevel) || 1
  const xpPercent = Math.min(100, Math.round((xp / xpGoal) * 100))
  const streak = Number(player.dayStreak ?? player.streakDays) || 0
  const taskStreak = Number(player.taskStreak) || 0

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="dashboard-eyebrow">Resumen de progreso</p>
          <h1>Dashboard</h1>
          <p className="dashboard-week-label">
            Mostrando información de la semana: {weekRangeLabel(weekStart)}
          </p>
        </div>
        <div className="dashboard-week-controls">
          <div className="dashboard-week-nav" aria-label="Navegación semanal">
            <button type="button" onClick={() => setWeekOffset((offset) => offset - 1)}>
              Semana anterior
            </button>
            <button type="button" onClick={() => setWeekOffset((offset) => offset + 1)}>
              Semana siguiente
            </button>
          </div>
          {weekOffset === 0 ? (
            <span className="dashboard-current-week">Semana actual</span>
          ) : (
            <button
              type="button"
              className="dashboard-current-week-button"
              onClick={() => setWeekOffset(0)}
            >
              Volver a la semana actual
            </button>
          )}
        </div>
      </header>

      {loadError && (
        <p className="dashboard-error" role="alert">
          {loadError.status === 401
            ? 'Tu sesión ha caducado. Vuelve a iniciar sesión.'
            : 'No se pudieron cargar los datos de esta semana. Inténtalo de nuevo más tarde.'}
        </p>
      )}

      <section className="dashboard-level-card" aria-labelledby="dashboard-level-title">
        <div className="dashboard-card-heading">
          <h2 id="dashboard-level-title">⭐ Nivel {level}</h2>
          <span>{xp} / {xpGoal} XP</span>
        </div>
        <div className="dashboard-progress-track" aria-label={`${xpPercent}% de progreso hacia el siguiente nivel`}>
          <div className="dashboard-progress-value" style={{ width: `${xpPercent}%` }} />
        </div>
        <p>{Math.max(0, xpGoal - xp)} XP para el nivel {level + 1}</p>
      </section>

      <section aria-labelledby="dashboard-summary-title">
        <h2 id="dashboard-summary-title" className="dashboard-section-title">✅ Cumplimiento</h2>
        <div className="dashboard-stat-grid">
          <StatCard icon="✅" value={summary.completed} label="Completadas" tone="success" />
          <StatCard icon="❌" value={summary.notDone} label="No hechas" tone="danger" />
          <StatCard icon="⏳" value={summary.pending} label="Pendientes" tone="gold" />
          <StatCard
            icon="📈"
            value={summary.percentage === null ? 'Sin datos' : `${summary.percentage}%`}
            label="Cumplimiento"
            tone="violet"
          />
        </div>
      </section>

      <section className="dashboard-content-grid" aria-label="Estadísticas semanales">
        <div className="dashboard-charts">
          <WeeklyBarChart title="XP por día" days={summary.xpByDay} valueKey="xp" unit="XP" />
          <WeeklyBarChart
            title="Oro ganado por día"
            days={summary.goldByDay}
            valueKey="gold"
            unit="🪙"
            barClassName="dashboard-chart-bar-gold"
          />
        </div>

        <div className="dashboard-side-panels">
          <div className="dashboard-highlight-card dashboard-streak-card">
            <div className="dashboard-streak-metric">
              <span className="dashboard-highlight-icon" aria-hidden="true">🔥</span>
              <div>
                <strong>{streak} días</strong>
                <span className="dashboard-streak-label">Racha diaria</span>
              </div>
            </div>
            <div className="dashboard-streak-metric dashboard-streak-metric-task">
              <span className="dashboard-highlight-icon" aria-hidden="true">✅</span>
              <div>
                <strong>{taskStreak}</strong>
                <span className="dashboard-streak-label">Racha de tareas</span>
              </div>
            </div>
          </div>
          <div className="dashboard-highlight-card dashboard-motivation-card">
            <span className="dashboard-highlight-icon">🌟</span>
            <div>
              <strong>{hasTasks ? '¡Sigue así!' : 'Empieza tu aventura'}</strong>
              <span>{hasTasks ? 'Cada misión cuenta para tu progreso.' : 'Crea tu primera misión para comenzar.'}</span>
            </div>
          </div>
        </div>
      </section>

      {!hasTasks && (
        <section className="dashboard-empty-state" aria-labelledby="dashboard-empty-title">
          <span className="dashboard-empty-icon">🗓️</span>
          <div>
            <h2 id="dashboard-empty-title">Todavía no tienes misiones</h2>
            <p>Crea tu primera misión y empieza a construir tu progreso.</p>
          </div>
          <button type="button" onClick={() => navigate('/home')}>
            Ir al calendario
          </button>
        </section>
      )}
    </main>
  )
}

function WeeklyBarChart({ title, days, valueKey, unit, barClassName = '' }) {
  const total = days.reduce((sum, day) => sum + day[valueKey], 0)
  const maxValue = Math.max(...days.map((day) => day[valueKey]), 1)
  const valuesLabel = days.map((day) => `${day.name}: ${day[valueKey]} ${unit}`).join(', ')

  return (
    <div className="dashboard-panel">
      <div className="dashboard-panel-heading">
        <h2>{title}</h2>
        <span>{total} {unit} esta semana</span>
      </div>
      <div className="dashboard-chart" role="img" aria-label={`${title}. ${valuesLabel}`}>
        {days.map((day) => (
          <div className="dashboard-chart-column" key={day.iso}>
            <span className="dashboard-chart-value">{day[valueKey]} {unit}</span>
            <div className="dashboard-chart-track">
              <div
                className={`dashboard-chart-bar ${barClassName}`}
                style={{
                  height: `${Math.max(day[valueKey] === 0 ? 0 : 8, (day[valueKey] / maxValue) * 100)}%`,
                }}
              />
            </div>
            <span className="dashboard-chart-day">
              <strong>{day.name}</strong>
              <small>{day.date.getDate()} {day.date.toLocaleDateString('es-ES', { month: 'short' }).replace('.', '')}</small>
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

function StatCard({ icon, value, label, tone }) {
  return (
    <article className={`dashboard-stat-card dashboard-stat-card-${tone}`}>
      <span className="dashboard-stat-icon" aria-hidden="true">{icon}</span>
      <strong>{value}</strong>
      <span>{label}</span>
    </article>
  )
}
