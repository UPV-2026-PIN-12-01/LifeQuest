import { getJson, playerUrl } from './http.js'
import { getTasksForWeek } from './tasks.js'
import { startOfWeek, toIsoDate } from '../utils/week.js'

export async function getPlayerProfile() {
  const profile = await getJson(playerUrl())
  return {
    username: profile.playerName || profile.username,
    profileUrl: profile.profileUrl || '',
    playerClass: profile.playerClass,
    level: profile.level,
    xp: profile.xp,
    xpForNextLevel: profile.xpForNextLevel,
    gold: profile.gold,
    streakDays: Number(profile.dayStreak ?? profile.streakDays) || 0,
    dayStreak: Number(profile.dayStreak ?? profile.streakDays) || 0,
    taskStreak: Number(profile.taskStreak) || 0,
    xpAccBoost: Number(profile.xpAccBoost) || 1,
  }
}

export async function getDailyQuests() {
  const url = playerUrl('/daily-quests')
  const cacheDate = toIsoDate(new Date())
  return getJson(url, { cacheKey: `${url}?cacheDate=${cacheDate}` })
}

export function preloadSessionData() {
  const weekStart = startOfWeek(new Date())
  return Promise.allSettled([
    getPlayerProfile(),
    getJson('/api/character/stats'),
    getJson('/api/users/me'),
    getDailyQuests(),
    getTasksForWeek(weekStart),
  ])
}
