import { getJson, playerUrl } from './http.js'

export async function getPlayerProfile() {
  const profile = await getJson(playerUrl())
  return {
    username: profile.playerName || profile.username,
    profileUrl: profile.profileUrl || '',
    playerClass: profile.playerClass,
    level: profile.level,
    xp: profile.xp,
    xpForNextLevel: profile.xpForNextLevel,
    streakDays: profile.streakDays,
  }
}

export async function getDailyQuests() {
  return getJson(playerUrl('/daily-quests'))
}
