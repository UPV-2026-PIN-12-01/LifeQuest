// Mock data until the backend exposes task endpoints. Keep the signatures when switching to fetch.
import { addDays, startOfWeek, toIsoDate } from '../utils/week.js'

const currentWeekStart = startOfWeek(new Date())

function dayOfCurrentWeek(offset) {
  return toIsoDate(addDays(currentWeekStart, offset))
}

const MOCK_TASKS = [
  {
    id: 1,
    title: 'Partida con amigos',
    icon: '🎮',
    color: 'purple',
    date: dayOfCurrentWeek(0),
    startTime: '16:00',
    endTime: '18:00',
    status: 'finished',
    priority: 'low',
  },
  {
    id: 2,
    title: 'Jugar al Rol',
    icon: '🎨',
    color: 'yellow',
    date: dayOfCurrentWeek(2),
    startTime: '19:00',
    endTime: '20:00',
    status: 'finished',
    priority: 'medium',
  },
  {
    id: 3,
    title: 'Rutina de gimnasio',
    icon: '💪',
    color: 'red',
    date: dayOfCurrentWeek(4),
    startTime: '11:00',
    endTime: '13:00',
    status: 'pending',
    priority: 'medium',
  },
]

const MOCK_SUMMARY = {
  activeChallenges: 3,
  trainingInProgress: 1,
  weeklyXp: 350,
}

export async function getTasksForWeek(weekStart) {
  const from = toIsoDate(weekStart)
  const to = toIsoDate(addDays(weekStart, 6))
  return MOCK_TASKS.filter((task) => task.date >= from && task.date <= to)
}

export async function getWeekSummary(weekStart) {
  const isCurrentWeek = toIsoDate(weekStart) === toIsoDate(currentWeekStart)
  return isCurrentWeek ? MOCK_SUMMARY : { activeChallenges: 0, trainingInProgress: 0, weeklyXp: 0 }
}
