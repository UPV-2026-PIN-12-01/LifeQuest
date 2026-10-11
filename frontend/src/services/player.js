// Mock data until the backend exposes player endpoints. Keep the signatures when switching to fetch.
const MOCK_PLAYER = {
  username: 'Aventurero',
  playerClass: 'Mago',
  level: 4,
  xp: 650,
  xpForNextLevel: 1000,
  streakDays: 4,
}

const MOCK_DAILY_QUESTS = [
  { id: 'login', title: 'Entra en LifeQuest', icon: '🌅', color: 'green', progress: 1, goal: 1, xp: 10 },
  { id: 'study', title: 'Completa 2 tareas de estudio', icon: '📚', color: 'purple', progress: 0, goal: 2, xp: 30 },
  { id: 'streak', title: 'Mantén la racha (5 días)', icon: '🔥', color: 'orange', progress: 4, goal: 5, xp: 25 },
]

export async function getPlayerProfile() {
  return MOCK_PLAYER
}

export async function getDailyQuests() {
  return MOCK_DAILY_QUESTS
}
