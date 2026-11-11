import { useEffect, useState } from 'react'
import {
  activityById,
  activityStatsLabel,
  priorityById,
  statusById,
} from '../../utils/taskOptions.js'
import TaskActions from './TaskActions.jsx'

// onTaskChanged receives the task the server returns after a check-in, completion or skip.
export default function TaskTable({ id, tasks, onTaskChanged, testingMode = false }) {
  // The id, not a copy, so the details follow the latest task from the server.
  const [selectedId, setSelectedId] = useState(null)
  const selectedTask = tasks.find((task) => task.id === selectedId) ?? null

  return (
    <section id={id} className="task-table-panel" aria-labelledby="task-table-title">
      <div className="task-table-heading">
        <div>
          <p className="section-kicker">Resumen semanal</p>
          <h2 id="task-table-title">Mis tareas</h2>
        </div>
        <span className="task-table-count">{tasks.length} tareas</span>
      </div>
      {tasks.length === 0 ? (
        <p className="task-table-empty">No hay tareas para esta semana.</p>
      ) : (
        <div className="task-table-scroll">
          <table className="task-table">
            <caption className="visually-hidden">Tareas de la semana actual</caption>
            <thead>
              <tr>
                <th scope="col">Tarea</th>
                <th scope="col">Fecha</th>
                <th scope="col">Horario</th>
                <th scope="col">Actividad / atributos</th>
                <th scope="col">Estado</th>
                <th scope="col">Prioridad</th>
              </tr>
            </thead>
            <tbody>
              {tasks
                .slice()
                .sort((a, b) => `${a.date} ${a.startTime}`.localeCompare(`${b.date} ${b.startTime}`))
                .map((task) => (
                  <tr key={task.id}>
                    <td>
                      <button
                        className="task-table-link"
                        type="button"
                        onClick={() => setSelectedId(task.id)}
                      >
                        <span className="task-table-icon" aria-hidden="true">{task.icon}</span>
                        <span>{task.title}</span>
                      </button>
                    </td>
                    <td>{formatDate(task.date)}</td>
                    <td className="tabular">{task.startTime}–{task.endTime}</td>
                    <td>
                      <ActivitySummary activityType={task.activityType} status={task.status} />
                    </td>
                    <td><TaskStatusBadge status={task.status} /></td>
                    <td>{priorityById(task.priority).label}</td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      )}
      {selectedTask && (
        <TaskDetailsModal
          task={selectedTask}
          testingMode={testingMode}
          onClose={() => setSelectedId(null)}
          onTaskChanged={onTaskChanged}
        />
      )}
    </section>
  )
}

function TaskDetailsModal({ task, onClose, onTaskChanged, testingMode }) {
  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === 'Escape') onClose()
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [onClose])

  return (
    <div className="task-modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className={`task-modal task-details-modal task-color-${task.color}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="task-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <button className="task-modal-close" type="button" onClick={onClose} aria-label="Cerrar detalles">
          ×
        </button>
        <span className="task-modal-icon" aria-hidden="true">{task.icon}</span>
        <p className="section-kicker">Detalles de la tarea</p>
        <h2 id="task-modal-title">{task.title}</h2>
        <p className="task-modal-description">{task.description || 'Sin descripción.'}</p>
        <dl className="task-modal-details">
          <div>
            <dt>Fecha</dt>
            <dd>{formatDate(task.date)}</dd>
          </div>
          <div>
            <dt>Horario</dt>
            <dd className="tabular">{task.startTime}–{task.endTime}</dd>
          </div>
          <div>
            <dt>Estado</dt>
            <dd><TaskStatusBadge status={task.status} /></dd>
          </div>
          <div>
            <dt>Prioridad</dt>
            <dd>{priorityById(task.priority).label}</dd>
          </div>
          <div>
            <dt>Actividad / atributos</dt>
            <dd><ActivitySummary activityType={task.activityType} status={task.status} /></dd>
          </div>
          <div>
            <dt>Experiencia</dt>
            <dd className="tabular">+{task.xp} XP</dd>
          </div>
          <div>
            <dt>Oro</dt>
            <dd className="tabular">+{task.gold} 💰</dd>
          </div>
        </dl>
        {onTaskChanged && (
          <TaskActions task={task} testingMode={testingMode} onChanged={onTaskChanged} />
        )}
        <button className="task-modal-action task-modal-action-secondary" type="button" onClick={onClose}>
          Cerrar
        </button>
      </section>
    </div>
  )
}

function TaskStatusBadge({ status: statusId }) {
  const status = statusById(statusId)
  return (
    <span className={`task-status-badge task-status-badge-${status.id}`}>
      <span aria-hidden="true">{status.icon}</span>
      {status.label}
    </span>
  )
}

function ActivitySummary({ activityType, status }) {
  const activity = activityById(activityType)
  return (
    <span className="task-activity-summary">
      <strong>{activity.label}</strong>
      <small>{status !== 'not_completed' ? activityStatsLabel(activityType) : 'No se obtuvieron beneficios'}</small>
    </span>
  )
}

function formatDate(isoDate) {
  return new Intl.DateTimeFormat('es-ES', {
    day: 'numeric',
    month: 'short',
  }).format(new Date(`${isoDate}T12:00:00`))
}
