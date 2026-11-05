import { getJson, playerUrl, requestJson } from './http.js'
import { getCachedDataValue, setCachedDataValue } from './dataCache.js'
import { addDays, toIsoDate } from '../utils/week.js'

const TASKS_CACHE_VERSION = 'v2'

export function tasksCacheKey(weekStart) {
  return playerUrl(`/tasks?${new URLSearchParams({
    from: toIsoDate(weekStart),
    to: toIsoDate(addDays(weekStart, 6)),
  })}`)
}

function tasksDataCacheKey(weekStart) {
  return `${tasksCacheKey(weekStart)}:${TASKS_CACHE_VERSION}`
}

export async function getTasksForWeek(weekStart) {
  const url = tasksCacheKey(weekStart)
  return getJson(url, { cacheKey: tasksDataCacheKey(weekStart) })
}

export function getCachedTasksForWeek(weekStart) {
  return getCachedDataValue(tasksDataCacheKey(weekStart))
}

export function setCachedTasksForWeek(weekStart, tasks) {
  setCachedDataValue(tasksDataCacheKey(weekStart), tasks)
}

// `task` includes title, description, icon, color, date, startTime, endTime, priority, xp and gold.
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

export async function getTaskTestingMode() {
  return requestJson(playerUrl('/tasks/testing-mode'))
}

export async function completeTaskInstantly(id) {
  return requestJson(playerUrl(`/tasks/${id}/complete-instant`), { method: 'POST' })
}

export async function skipTask(id) {
  return requestJson(playerUrl(`/tasks/${id}/skip`), { method: 'POST' })
}
