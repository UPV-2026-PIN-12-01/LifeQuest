import { getJson, playerUrl, requestJson } from './http.js'

export function getGoals(history = false) {
  const params = new URLSearchParams({ history: String(history) })
  return getJson(playerUrl(`/goals?${params}`))
}

export function getGoalQuota() {
  return getJson(playerUrl('/goals/quota'))
}

export function createGoal(goal) {
  return requestJson(playerUrl('/goals'), { method: 'POST', body: goal })
}

export function evaluateGoal(id, evaluation) {
  return requestJson(playerUrl(`/goals/${id}/evaluate`), {
    method: 'PATCH',
    body: evaluation,
  })
}

export function claimGoal(id) {
  return requestJson(playerUrl(`/goals/${id}/claim`), { method: 'POST' })
}

export function deleteGoal(id) {
  return requestJson(playerUrl(`/goals/${id}`), { method: 'DELETE' })
}
