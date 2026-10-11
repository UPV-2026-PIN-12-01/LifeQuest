import { priorityById, statusById } from '../../utils/taskOptions.js'

// Priority is the left bar, status is the corner marker; both are spelled out for screen readers.
export default function TaskCard({ task }) {
  const status = statusById(task.status)
  const priority = priorityById(task.priority)

  return (
    <article
      className={`task-card task-color-${task.color} task-priority-${priority.id} task-status-${status.id}`}
      title={`${task.title} · ${status.label} · Prioridad ${priority.label.toLowerCase()}`}
    >
      <span className="task-icon" aria-hidden="true">{task.icon}</span>
      <div className="task-body">
        <h3 className="task-title">{task.title}</h3>
        <p className="task-time">
          {task.startTime}–{task.endTime}
        </p>
        <span className="visually-hidden">
          {status.label}. Prioridad {priority.label.toLowerCase()}.
        </span>
      </div>
      <span className="task-status-marker" aria-hidden="true">{status.icon}</span>
    </article>
  )
}
