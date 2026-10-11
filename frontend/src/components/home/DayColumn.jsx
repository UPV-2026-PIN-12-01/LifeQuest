import TaskCard from './TaskCard.jsx'

export default function DayColumn({ day, tasks, isToday }) {
  const done = tasks.filter((task) => task.status === 'completed')
  const earnedXp = done.reduce((sum, task) => sum + task.xp, 0)
  const openXp = tasks
    .filter((task) => task.status === 'pending' || task.status === 'in_progress')
    .reduce((sum, task) => sum + task.xp, 0)
  const isPerfect = tasks.length > 0 && done.length === tasks.length
  const percent = tasks.length === 0 ? 0 : Math.round((done.length / tasks.length) * 100)

  return (
    <section className={`day-column${isToday ? ' is-today' : ''}`} aria-current={isToday ? 'date' : undefined}>
      <header className="day-header">
        <div className="day-date">
          <span className="day-name">{day.name}</span>
          <span className="day-number">{day.date.getDate()}</span>
          {isToday && <span className="day-tag day-tag-today">Hoy</span>}
          {isPerfect && <span className="day-tag day-tag-perfect">⭐ Perfecto</span>}
        </div>
        {tasks.length > 0 && (
          <>
            <div
              className="day-progress"
              role="progressbar"
              aria-label="Tareas completadas"
              aria-valuemin={0}
              aria-valuemax={tasks.length}
              aria-valuenow={done.length}
            >
              <span className="day-progress-fill" style={{ width: `${percent}%` }} />
            </div>
            <p className="day-stats">
              <span>
                {done.length}/{tasks.length} hechas
              </span>
              <span>{isToday && openXp > 0 ? `${openXp} XP en juego` : `+${earnedXp} XP`}</span>
            </p>
          </>
        )}
      </header>
      <div className="day-tasks">
        {tasks.length === 0 ? (
          <p className="day-empty">
            <span aria-hidden="true">🏕️</span>
            Día de descanso
          </p>
        ) : (
          tasks.map((task) => <TaskCard key={task.id} task={task} />)
        )}
      </div>
    </section>
  )
}
