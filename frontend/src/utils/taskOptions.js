export const TASK_STATUSES = [
  { id: 'pending', label: 'Pendiente', icon: '⏳' },
  { id: 'in_progress', label: 'En progreso', icon: '▶' },
  { id: 'finished', label: 'Finalizada', icon: '⚠️' },
  { id: 'completed', label: 'Completada', icon: '✓' },
  { id: 'not_completed', label: 'No completada', icon: '✕' },
]

export const TASK_PRIORITIES = [
  { id: 'low', label: 'Baja', icon: '▽' },
  { id: 'medium', label: 'Media', icon: '◈' },
  { id: 'high', label: 'Alta', icon: '▲' },
]

export function statusById(id) {
  return TASK_STATUSES.find((status) => status.id === id) ?? TASK_STATUSES[0]
}

export function priorityById(id) {
  return TASK_PRIORITIES.find((priority) => priority.id === id) ?? TASK_PRIORITIES[0]
}
