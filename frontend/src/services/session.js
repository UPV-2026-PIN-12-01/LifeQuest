// Temporary until login exists: the current user comes from #/home?userId=N and is remembered in localStorage.
const STORAGE_KEY = 'lifequest.userId'

export function getCurrentUserId() {
  const query = window.location.hash.split('?')[1] ?? ''
  const fromUrl = new URLSearchParams(query).get('userId')
  if (fromUrl) {
    try {
      localStorage.setItem(STORAGE_KEY, fromUrl)
    } catch {
      // Storage can be blocked; the URL still works for this visit.
    }
    return fromUrl
  }
  try {
    return localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}
