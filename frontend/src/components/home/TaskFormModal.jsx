import { useEffect, useRef, useState } from 'react'
import { createTask, deleteTask, updateTask } from '../../services/tasks.js'
import { TASK_COLORS, TASK_ICONS, TASK_PRIORITIES, XP_BY_PRIORITY } from '../../utils/taskOptions.js'
import { addDays, toIsoDate } from '../../utils/week.js'

const TITLE_MAX = 80
// Same limits as TaskService in the backend, checked here so the user sees why before sending.
const PAST_START_GRACE_MS = 15 * 60_000
const MIN_DURATION_MS = 15 * 60_000

// Backend error messages for the task form, in the user's language.
const SERVER_ERRORS = {
  'Task cannot start in the past': 'La misión no puede empezar en el pasado.',
  'Task must last at least 15 minutes': 'La misión tiene que durar al menos 15 minutos.',
  'Task cannot cross midnight UTC': 'De momento una misión no puede cruzar la medianoche UTC. Ajusta las horas.',
  'Task cannot be edited once it has started': 'Una misión que ya ha empezado no se puede editar.',
  'endTime must be after startTime': 'La hora de fin tiene que ser posterior a la de inicio.',
}

// Create a task when `task` is null, otherwise edit or delete it. Calls onSaved after any change.
export default function TaskFormModal({ task, defaultDate, onClose, onSaved }) {
  const dialogRef = useRef(null)
  const [form, setForm] = useState(() => initialForm(task, defaultDate))
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const isEdit = Boolean(task)

  useEffect(() => {
    const dialog = dialogRef.current
    if (dialog && !dialog.open) dialog.showModal()
  }, [])

  function update(field, value) {
    setForm((current) => ({ ...current, [field]: value }))
    setError(null)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const problem = validate(form)
    if (problem) {
      setError(problem)
      return
    }
    setBusy(true)
    try {
      const body = { ...form, title: form.title.trim(), description: form.description.trim() }
      if (isEdit) {
        await updateTask(task.id, body)
      } else {
        await createTask(body)
      }
      onSaved()
    } catch (requestError) {
      setError(requestErrorMessage(requestError))
      setBusy(false)
    }
  }

  async function handleDelete() {
    if (!confirmDelete) {
      setConfirmDelete(true)
      return
    }
    setBusy(true)
    try {
      await deleteTask(task.id)
      onSaved()
    } catch (requestError) {
      setError(requestErrorMessage(requestError))
      setBusy(false)
    }
  }

  return (
    // The native dialog closes on Escape; onClose keeps React state in sync.
    <dialog
      ref={dialogRef}
      className="task-modal"
      aria-labelledby="task-modal-title"
      onClose={onClose}
      onClick={(event) => {
        if (event.target === dialogRef.current) dialogRef.current.close()
      }}
    >
      <form className="task-form" onSubmit={handleSubmit} noValidate>
        <header className="task-form-header">
          <h2 id="task-modal-title">{isEdit ? 'Editar misión' : 'Nueva misión'}</h2>
          <button
            type="button"
            className="task-form-close"
            onClick={() => dialogRef.current.close()}
            aria-label="Cerrar"
          >
            ×
          </button>
        </header>

        <label className="task-field">
          <span>Título</span>
          <input
            type="text"
            value={form.title}
            maxLength={TITLE_MAX}
            onChange={(event) => update('title', event.target.value)}
            placeholder="Repasar el tema 3"
            required
            autoFocus
          />
        </label>

        <label className="task-field">
          <span>Descripción (opcional)</span>
          <textarea
            rows={2}
            value={form.description}
            onChange={(event) => update('description', event.target.value)}
          />
        </label>

        <div className="task-field-row">
          <label className="task-field">
            <span>Día</span>
            <input type="date" value={form.date} onChange={(event) => update('date', event.target.value)} required />
          </label>
          <label className="task-field">
            <span>Inicio</span>
            <input
              type="time"
              value={form.startTime}
              onChange={(event) => update('startTime', event.target.value)}
              required
            />
          </label>
          <label className="task-field">
            <span>Fin</span>
            <input
              type="time"
              value={form.endTime}
              onChange={(event) => update('endTime', event.target.value)}
              required
            />
          </label>
        </div>

        <fieldset className="task-field">
          <legend>Prioridad</legend>
          <div className="task-choice-row">
            {TASK_PRIORITIES.map((priority) => (
              <label key={priority.id} className="task-choice">
                <input
                  type="radio"
                  name="priority"
                  value={priority.id}
                  checked={form.priority === priority.id}
                  onChange={() => update('priority', priority.id)}
                />
                <span>
                  {priority.label} <small className="tabular">+{XP_BY_PRIORITY[priority.id]} XP</small>
                </span>
              </label>
            ))}
          </div>
        </fieldset>

        <fieldset className="task-field">
          <legend>Icono</legend>
          <div className="task-choice-row">
            {TASK_ICONS.map((icon) => (
              <label key={icon} className="task-choice task-choice-icon">
                <input
                  type="radio"
                  name="icon"
                  value={icon}
                  checked={form.icon === icon}
                  onChange={() => update('icon', icon)}
                />
                <span>{icon}</span>
              </label>
            ))}
          </div>
        </fieldset>

        <fieldset className="task-field">
          <legend>Color</legend>
          <div className="task-choice-row">
            {TASK_COLORS.map((color) => (
              <label key={color.id} className={`task-choice task-choice-color task-color-${color.id}`}>
                <input
                  type="radio"
                  name="color"
                  value={color.id}
                  checked={form.color === color.id}
                  onChange={() => update('color', color.id)}
                  aria-label={color.label}
                />
                <span aria-hidden="true" />
              </label>
            ))}
          </div>
        </fieldset>

        {error && (
          <p className="task-form-error" role="alert">
            {error}
          </p>
        )}

        <footer className="task-form-actions">
          {isEdit && (
            <button type="button" className="task-form-delete" onClick={handleDelete} disabled={busy}>
              {confirmDelete ? '¿Seguro? Eliminar' : 'Eliminar'}
            </button>
          )}
          <button type="button" className="task-form-cancel" onClick={() => dialogRef.current.close()} disabled={busy}>
            Cancelar
          </button>
          <button type="submit" className="task-form-save" disabled={busy}>
            {isEdit ? 'Guardar cambios' : 'Crear misión'}
          </button>
        </footer>
      </form>
    </dialog>
  )
}

function initialForm(task, defaultDate) {
  if (task) {
    return {
      title: task.title,
      description: task.description ?? '',
      icon: task.icon || TASK_ICONS[0],
      color: task.color,
      date: task.date,
      startTime: task.startTime,
      endTime: task.endTime,
      priority: task.priority,
    }
  }
  return {
    title: '',
    description: '',
    icon: TASK_ICONS[0],
    color: TASK_COLORS[0].id,
    ...defaultSlot(defaultDate),
    priority: 'medium',
  }
}

// The next full hour of defaultDate when it is today and still fits the day, else 09:00 of the next free day.
function defaultSlot(defaultDate) {
  const now = new Date()
  const nextHour = now.getHours() + 1
  if (defaultDate !== toIsoDate(now)) {
    return { date: defaultDate, startTime: '09:00', endTime: '10:00' }
  }
  if (nextHour <= 22) {
    const pad = (hour) => `${String(hour).padStart(2, '0')}:00`
    return { date: defaultDate, startTime: pad(nextHour), endTime: pad(nextHour + 1) }
  }
  return { date: toIsoDate(addDays(now, 1)), startTime: '09:00', endTime: '10:00' }
}

function validate(form) {
  if (!form.title.trim()) return 'Ponle un título a la misión.'
  if (!form.date || !form.startTime || !form.endTime) return 'Elige el día y las horas.'
  if (form.endTime <= form.startTime) return 'La hora de fin tiene que ser posterior a la de inicio.'
  // The database checks that start and end fall on the same day in UTC.
  const start = new Date(`${form.date}T${form.startTime}`)
  const end = new Date(`${form.date}T${form.endTime}`)
  if (start.getUTCDate() !== end.getUTCDate()) {
    return SERVER_ERRORS['Task cannot cross midnight UTC']
  }
  if (end - start < MIN_DURATION_MS) return SERVER_ERRORS['Task must last at least 15 minutes']
  if (start.getTime() < Date.now() - PAST_START_GRACE_MS) return SERVER_ERRORS['Task cannot start in the past']
  return null
}

function requestErrorMessage(error) {
  if (error.status === 401) return 'Tu sesión ha caducado. Vuelve a iniciar sesión.'
  if (error.status === 404) return 'Esta misión ya no existe.'
  if (SERVER_ERRORS[error.message]) return SERVER_ERRORS[error.message]
  if (error.status === 400) return 'Revisa los datos de la misión.'
  return 'No se pudo guardar. Inténtalo de nuevo más tarde.'
}
