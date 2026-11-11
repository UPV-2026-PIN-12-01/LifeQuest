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

export const TASK_ACTIVITY_TYPES = [
  { id: 'EXERCISE', label: 'Ejercicio', stats: [['Fuerza', 2], ['Estamina', 1]] },
  { id: 'STUDY', label: 'Estudio', stats: [['Conocimiento', 3]] },
  { id: 'WORK', label: 'Trabajo', stats: [['Vida', 1], ['Fuerza', 1], ['Conocimiento', 1]] },
  { id: 'SOCIAL', label: 'Social', stats: [['Vida', 2], ['Velocidad', 1]] },
  { id: 'SPORTS', label: 'Deporte', stats: [['Velocidad', 1], ['Estamina', 2]] },
  { id: 'ENTERTAINMENT', label: 'Entretenimiento', stats: [['Velocidad', 2]] },
]

const NO_ACTIVITY_TYPE = { id: 'UNDEFINED', label: 'Sin tipo', stats: [] }

export function statusById(id) {
  return TASK_STATUSES.find((status) => status.id === id) ?? TASK_STATUSES[0]
}

export function activityById(id) {
  return TASK_ACTIVITY_TYPES.find((activity) => activity.id === id) ?? NO_ACTIVITY_TYPE
}

export function activityStatsLabel(activityType) {
  const { stats } = activityById(activityType)
  return stats.length > 0
    ? stats.map(([name, value]) => `+${value} ${name}`).join(' · ')
    : 'Sin aumento de atributos'
}

export function priorityById(id) {
  return TASK_PRIORITIES.find((priority) => priority.id === id) ?? TASK_PRIORITIES[0]
}

// Same values as the chk_task_color constraint in the tasks table.
export const TASK_COLORS = [
  { id: 'purple', label: 'Morado' },
  { id: 'blue', label: 'Azul' },
  { id: 'green', label: 'Verde' },
  { id: 'yellow', label: 'Amarillo' },
  { id: 'red', label: 'Rojo' },
]

export const TASK_ICONS = ['📆', '📘', '📝', '🧪', '💻', '💪', '🏃', '🧹', '🎨', '🎮']

// XP of the reward the backend creates, by priority.
export const XP_BY_PRIORITY = { low: 10, medium: 20, high: 30 }

export function taskDurationMultiplier(durationMinutes) {
  if (!Number.isFinite(durationMinutes) || durationMinutes < 30) return 1
  return Math.min(2, 1.1 + ((durationMinutes - 30) / 150) * 0.9)
}
