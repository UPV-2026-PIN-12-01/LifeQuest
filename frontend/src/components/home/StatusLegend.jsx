import { TASK_PRIORITIES, TASK_STATUSES } from '../../utils/taskOptions.js'

export default function StatusLegend() {
  return (
    <div className="legend">
      <ul className="legend-group" aria-label="Leyenda de estados">
        {TASK_STATUSES.map((status) => (
          <li key={status.id} className={`legend-item task-status-${status.id}`}>
            <span className="task-status-marker" aria-hidden="true">{status.icon}</span>
            {status.label}
          </li>
        ))}
      </ul>
      <ul className="legend-group" aria-label="Leyenda de prioridades">
        {TASK_PRIORITIES.map((priority) => (
          <li key={priority.id} className={`legend-item task-priority-${priority.id}`}>
            <span className="legend-bar" aria-hidden="true" />
            Prioridad {priority.label.toLowerCase()}
          </li>
        ))}
      </ul>
    </div>
  )
}
