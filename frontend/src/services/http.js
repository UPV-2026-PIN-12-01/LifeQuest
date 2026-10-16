import { clearSession, getAccessToken } from './session.js'

export async function getJson(url) {
  return requestJson(url)
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
    response = await fetch(url, {
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
  return payload
}

export function playerUrl(path = '') {
  return `/api/players/me${path}`
}
