const STORAGE_KEY = 'lifequest.data-cache'
const memoryCache = new Map()
const pendingRequests = new Map()
let cacheGeneration = 0

export function getCachedData(key, load) {
  if (memoryCache.has(key)) {
    return Promise.resolve(memoryCache.get(key))
  }

  const stored = readStoredCache()
  if (Object.hasOwn(stored, key)) {
    memoryCache.set(key, stored[key])
    return Promise.resolve(stored[key])
  }

  if (pendingRequests.has(key)) {
    return pendingRequests.get(key)
  }

  const generation = cacheGeneration
  const request = load()
    .then((data) => {
      if (generation === cacheGeneration) {
        memoryCache.set(key, data)
        const next = readStoredCache()
        next[key] = data
        writeStoredCache(next)
      }
      return data
    })
    .finally(() => {
      if (pendingRequests.get(key) === request) {
        pendingRequests.delete(key)
      }
    })

  pendingRequests.set(key, request)
  return request
}

export function invalidateCachedData(...prefixes) {
  if (prefixes.length === 0) return

  cacheGeneration++
  pendingRequests.clear()
  for (const key of memoryCache.keys()) {
    if (prefixes.some((prefix) => key.startsWith(prefix))) {
      memoryCache.delete(key)
    }
  }

  const stored = readStoredCache()
  for (const key of Object.keys(stored)) {
    if (prefixes.some((prefix) => key.startsWith(prefix))) {
      delete stored[key]
    }
  }
  writeStoredCache(stored)
}

export function clearCachedData() {
  cacheGeneration++
  memoryCache.clear()
  pendingRequests.clear()
  try {
    sessionStorage.removeItem(STORAGE_KEY)
  } catch {
    // The in-memory cache is still cleared if browser storage is unavailable.
  }
}

function readStoredCache() {
  try {
    const stored = JSON.parse(sessionStorage.getItem(STORAGE_KEY) || '{}')
    return stored && typeof stored === 'object' && !Array.isArray(stored) ? stored : {}
  } catch {
    try {
      sessionStorage.removeItem(STORAGE_KEY)
    } catch {
      // Storage is optional; the in-memory cache remains available.
    }
    return {}
  }
}

function writeStoredCache(cache) {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(cache))
  } catch {
    // Storage is optional; the in-memory cache remains available.
  }
}
