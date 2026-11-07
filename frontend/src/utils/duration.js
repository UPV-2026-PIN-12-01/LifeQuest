// 90 -> "1 h 30 min"; 0 or missing -> null so callers can skip it.
export function formatDuration(minutes) {
  if (!minutes) return null
  const hours = Math.floor(minutes / 60)
  const rest = minutes % 60
  if (!hours) return `${rest} min`
  return rest ? `${hours} h ${rest} min` : `${hours} h`
}
