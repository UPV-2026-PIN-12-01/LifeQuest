// Mock profile until the backend exposes a player endpoint. Keep the signature when switching to fetch.
const MOCK_PLAYER = {
  username: 'Aventurero',
  playerClass: 'Mago',
  level: 4,
  xp: 650,
  xpForNextLevel: 1000,
}

export async function getPlayerProfile() {
  return MOCK_PLAYER
}
