import { clearSession, getAccessToken } from './session.js'
import { getCachedData, invalidateCachedData } from './dataCache.js'

const API_ORIGIN = (
  import.meta.env.VITE_API_URL
  ?? (import.meta.env.PROD ? 'https://lifequest.up.railway.app' : '')
).replace(/\/$/, '')

export function apiUrl(path) {
  return `${API_ORIGIN}${path}`
}

export async function getJson(url, { cacheKey = url } = {}) {
  return getCachedData(cacheKey, () => requestJson(url))
}

// Sends `body` as JSON when given; a 204 or empty response resolves to null.
export async function requestJson(url, { method = 'GET', body } = {}) {
  const token = getAccessToken()
  if (!token) {
    const error = new Error('Not logged in')
    error.status = 401
    throw error
  }
  const headers = { Authorization: `Bearer ${token}` }
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  let response
  try {
    response = await fetch(apiUrl(url), {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
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
  if (method !== 'GET') {
    invalidateAfterMutation(url)
  }
  return payload
}

export async function patchJson(url, body) {
  const token = getAccessToken()
  if (!token) {
    const error = new Error('Not logged in')
    error.status = 401
    throw error
  }
  let response
  try {
    response = await fetch(apiUrl(url), {
      method: 'PATCH',
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(body),
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
  invalidateAfterMutation(url)
  return payload
}

export async function patchForm(url, formData) {
  const token = getAccessToken()
  if (!token) {
    const error = new Error('Not logged in')
    error.status = 401
    throw error
  }
  let response
  try {
    response = await fetch(apiUrl(url), {
      method: 'PATCH',
      headers: { Authorization: `Bearer ${token}` },
      body: formData,
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
  invalidateAfterMutation(url)
  return payload
}

function invalidateAfterMutation(url) {
  const path = url.split('?')[0]
  if (path.startsWith('/api/players/me/challenges')) {
    invalidateCachedData('/api/players/me/challenges', '/api/players/me', '/api/character/stats')
    return
  }
  if (path.startsWith('/api/players/me/tasks')) {
    invalidateCachedData('/api/players/me/tasks')
    const action = path.slice('/api/players/me/tasks'.length).split('/').filter(Boolean).at(-1)
    if (['start', 'heartbeat'].includes(action)) return

    invalidateCachedData('/api/players/me/daily-quests')
    if (action !== undefined && !['start', 'heartbeat'].includes(action)) {
      invalidateCachedData('/api/players/me', '/api/character/stats')
    }
    return
  }
  if (path.startsWith('/api/users/me')) {
    invalidateCachedData('/api/users/me', '/api/players/me')
    return
  }
  if (path === '/api/character/stats') {
    invalidateCachedData('/api/character/stats', '/api/players/me')
  }
}

export function playerUrl(path = '') {
  return `/api/players/me${path}`
}
