import { priorityById, statusById } from '../../utils/taskOptions.js'

const STATUS_NOTES = {
  in_progress: '▶ En curso',
  finished: '¿La completaste?',
}

export default function TaskCard({ task, onSelect }) {
  const status = statusById(task.status)
  const priority = priorityById(task.priority)
  const isCompleted = status.id === 'completed'

  return (
    <article
      className={`task-card task-color-${task.color} task-status-${status.id}`}
      title={`${task.title} · ${status.label} · Prioridad ${priority.label.toLowerCase()}`}
      role={onSelect ? 'button' : undefined}
      tabIndex={onSelect ? 0 : undefined}
      onClick={onSelect ? () => onSelect(task) : undefined}
      onKeyDown={
        onSelect
          ? (event) => {
              if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault()
                onSelect(task)
              }
            }
          : undefined
      }
    >
      <div className="task-top">
        <span className="task-icon" aria-hidden="true">{task.icon}</span>
        <h3 className="task-title">
          {priority.id === 'high' && (
            <span className="task-high" aria-hidden="true">
              !{' '}
            </span>
          )}
          {task.title}
        </h3>
        {isCompleted && (
          <span className="task-check" aria-hidden="true">
            ✓
          </span>
        )}
      </div>
      {STATUS_NOTES[status.id] && <p className="task-note">{STATUS_NOTES[status.id]}</p>}
      <div className="task-meta">
        <span className="task-time tabular">
          {task.startTime}–{task.endTime}
        </span>
        <span className="task-xp tabular">+{task.xp}</span>
      </div>
      <span className="visually-hidden">
        {status.label}. Prioridad {priority.label.toLowerCase()}. {task.xp} XP.
      </span>
    </article>
  )
}
