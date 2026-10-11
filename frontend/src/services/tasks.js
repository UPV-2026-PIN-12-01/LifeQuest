// Mock data until the backend exposes task endpoints. Keep the signatures when switching to fetch.
import { addDays, startOfWeek, toIsoDate } from '../utils/week.js'

const currentWeekStart = startOfWeek(new Date())

function dayOfCurrentWeek(offset) {
  return toIsoDate(addDays(currentWeekStart, offset))
}

const MOCK_TASKS = [
  {
    id: 1,
    title: 'Repasar Cálculo',
    icon: '📘',
    color: 'purple',
    date: dayOfCurrentWeek(0),
    startTime: '09:00',
    endTime: '10:30',
    status: 'completed',
    priority: 'medium',
    xp: 50,
  },
  {
    id: 2,
    title: 'Correr 5 km',
    icon: '🏃',
    color: 'red',
    date: dayOfCurrentWeek(0),
    startTime: '18:00',
    endTime: '18:45',
    status: 'completed',
    priority: 'low',
    xp: 30,
  },
  {
    id: 3,
    title: 'Práctica de Física',
    icon: '🧪',
    color: 'blue',
    date: dayOfCurrentWeek(1),
    startTime: '10:00',
    endTime: '12:00',
    status: 'completed',
    priority: 'medium',
    xp: 60,
  },
  {
    id: 4,
    title: 'Leer capítulo 4',
    icon: '📖',
    color: 'yellow',
    date: dayOfCurrentWeek(1),
    startTime: '20:00',
    endTime: '21:00',
    status: 'not_completed',
    priority: 'low',
    xp: 40,
  },
  {
    id: 5,
    title: 'Proyecto PIN',
    icon: '💻',
    color: 'yellow',
    date: dayOfCurrentWeek(2),
    startTime: '16:00',
    endTime: '19:00',
    status: 'finished',
    priority: 'high',
    xp: 80,
  },
  {
    id: 6,
    title: 'Rutina de gimnasio',
    icon: '💪',
    color: 'red',
    date: dayOfCurrentWeek(4),
    startTime: '11:00',
    endTime: '13:00',
    status: 'in_progress',
    priority: 'medium',
    xp: 40,
  },
  {
    id: 7,
    title: 'Examen de prueba',
    icon: '📝',
    color: 'purple',
    date: dayOfCurrentWeek(6),
    startTime: '10:00',
    endTime: '11:30',
    status: 'pending',
    priority: 'high',
    xp: 100,
  },
  {
    id: 8,
    title: 'Meditar',
    icon: '🧘',
    color: 'green',
    date: dayOfCurrentWeek(6),
    startTime: '22:00',
    endTime: '22:15',
    status: 'pending',
    priority: 'low',
    xp: 10,
  },
]

export async function getTasksForWeek(weekStart) {
  const from = toIsoDate(weekStart)
  const to = toIsoDate(addDays(weekStart, 6))
  return MOCK_TASKS.filter((task) => task.date >= from && task.date <= to)
}
