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

// Check-in: opens the task's session. The server keeps every time, the client only says what happened.
export async function startTask(id) {
  return requestJson(playerUrl(`/tasks/${id}/start`), { method: 'POST' })
}

// Sent about every minute while the page is visible; counts as focus time.
export async function sendTaskHeartbeat(id) {
  return requestJson(playerUrl(`/tasks/${id}/heartbeat`), { method: 'POST' })
}

export async function completeTask(id) {
  return requestJson(playerUrl(`/tasks/${id}/complete`), { method: 'POST' })
}

export async function skipTask(id) {
  return requestJson(playerUrl(`/tasks/${id}/skip`), { method: 'POST' })
}
