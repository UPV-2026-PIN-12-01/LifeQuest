import { getJson, playerUrl, requestJson } from './http.js'

const achievementsUrl = playerUrl('/achievements')

export function getAchievements() {
  return getJson(achievementsUrl, { cacheKey: achievementsUrl })
}

export function claimAchievement(indexId) {
  return requestJson(`${achievementsUrl}/${indexId}/claim`, { method: 'POST' })
}
