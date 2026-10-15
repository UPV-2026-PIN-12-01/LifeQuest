import { clearSession, getAccessToken } from './session.js'

export async function getJson(url) {
  const token = getAccessToken()
  if (!token) {
    const error = new Error('Not logged in')
    error.status = 401
    throw error
  }
  let response
  try {
    response = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` },
    })
  } catch {
    const error = new Error('Network error')
    error.status = 0
    throw error
  }
  if (response.status === 401) {
    clearSession()
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
  return `/api/players/me${path}`
}
