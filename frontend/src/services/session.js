// Supabase session returned by /api/auth/login, kept for this browser tab.
const STORAGE_KEY = 'lifequest.session'

export function saveSession(session) {
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
