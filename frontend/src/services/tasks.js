import { getJson, playerUrl } from './http.js'
import { addDays, toIsoDate } from '../utils/week.js'

export async function getTasksForWeek(weekStart) {
  const params = new URLSearchParams({
    from: toIsoDate(weekStart),
    to: toIsoDate(addDays(weekStart, 6)),
  })
  return getJson(playerUrl(`/tasks?${params}`))
}
