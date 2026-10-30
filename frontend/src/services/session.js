// Supabase session returned by /api/auth/login, kept for this browser tab.
import { clearCachedData } from './dataCache.js'

const STORAGE_KEY = 'lifequest.session'

export function saveSession(session) {
  clearCachedData()
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(session))
  } catch {
    // Storage can be blocked; the user will be asked to log in again.
  }
}

export function readSession() {
  try {
    return JSON.parse(sessionStorage.getItem(STORAGE_KEY) || 'null')
  } catch {
    clearSession()
    return null
  }
}

export function clearSession() {
  clearCachedData()
  try {
    sessionStorage.removeItem(STORAGE_KEY)
  } catch {
    // Nothing stored.
  }
}

export function getAccessToken() {
  return readSession()?.access_token ?? null
}

export function hasSession() {
  return Boolean(getAccessToken())
}
