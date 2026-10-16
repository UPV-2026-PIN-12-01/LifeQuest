import { getJson, playerUrl, requestJson } from './http.js'
import { addDays, toIsoDate } from '../utils/week.js'

export async function getTasksForWeek(weekStart) {
  const params = new URLSearchParams({
    from: toIsoDate(weekStart),
    to: toIsoDate(addDays(weekStart, 6)),
  })
  return getJson(playerUrl(`/tasks?${params}`))
}

// `task` has title, description, icon, color, date (yyyy-MM-dd), startTime, endTime (HH:mm) and priority.
export async function createTask(task) {
  return requestJson(playerUrl('/tasks'), { method: 'POST', body: task })
}

export async function updateTask(id, task) {
  return requestJson(playerUrl(`/tasks/${id}`), { method: 'PUT', body: task })
}

export async function deleteTask(id) {
  return requestJson(playerUrl(`/tasks/${id}`), { method: 'DELETE' })
}
