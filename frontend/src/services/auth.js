import { apiUrl } from './http.js'
import { clearSession, readSession } from './session.js'

export async function signup(formData) {
  let response
  try {
    response = await fetch(apiUrl('/api/auth/signup'), {
      method: 'POST',
      body: formData,
    })
  } catch {
    const error = new Error('Network error')
    error.status = 0
    error.payload = { error: 'Network error' }
    throw error
  }
  let payload = null
  try {
    payload = await response.json()
  } catch {
    payload = null
  }
  if (!response.ok) {
    const error = new Error(payload?.error || 'Signup failed')
    error.status = response.status
    error.payload = payload
    throw error
  }
  return payload
}

export async function login(email, password) {
  let response
  try {
    response = await fetch(apiUrl('/api/auth/login'), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({ email, password }),
    })
  } catch {
    const error = new Error('Network error')
    error.status = 0
    error.payload = { error: 'Network error' }
    throw error
  }
  let payload = null
  try {
    payload = await response.json()
  } catch {
    payload = null
  }
  if (!response.ok) {
    const error = new Error(payload?.error || 'Login failed')
    error.status = response.status
    error.payload = payload
    throw error
  }
  return payload
}

export async function getCurrentUser() {
  const session = readSession()
  if (!session?.access_token) {
    return null
  }
  const response = await fetch(apiUrl('/api/auth/me'), {
    headers: {
      Authorization: `Bearer ${session.access_token}`,
    },
  })
  if (response.status === 401) {
    clearSession()
    return null
  }
  if (!response.ok) {
    throw new Error('Could not load the current user')
  }
  return response.json()
}
