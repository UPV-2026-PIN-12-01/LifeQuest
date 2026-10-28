import { getJson, playerUrl, requestJson } from './http.js'

export function getChallenges(history = false) {
  const params = new URLSearchParams({ history: String(history) })
  return getJson(playerUrl(`/challenges?${params}`))
}

export function createChallenge(challenge) {
  return requestJson(playerUrl('/challenges'), { method: 'POST', body: challenge })
}

export function evaluateChallenge(id, evaluation) {
  return requestJson(playerUrl(`/challenges/${id}/evaluate`), {
    method: 'POST',
    body: evaluation,
  })
}

export function claimChallenge(id) {
  return requestJson(playerUrl(`/challenges/${id}/claim`), { method: 'POST' })
}

export function deleteChallenge(id) {
  return requestJson(playerUrl(`/challenges/${id}`), { method: 'DELETE' })
}
