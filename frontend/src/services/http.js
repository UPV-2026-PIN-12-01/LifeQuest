import { getCurrentUserId } from './session.js'

export async function getJson(url) {
  let response
  try {
    response = await fetch(url)
  } catch {
    const error = new Error('Network error')
    error.status = 0
    throw error
  }
  let payload = null
  try {
    payload = await response.json()
  } catch {
    payload = null
  }
  if (!response.ok) {
    const error = new Error(payload?.error || 'Request failed')
    error.status = response.status
    throw error
  }
  return payload
}

export function playerUrl(path = '') {
  const userId = getCurrentUserId()
  if (!userId) {
    const error = new Error('No user selected')
    error.status = 401
    throw error
  }
  return `/api/players/${encodeURIComponent(userId)}${path}`
}
