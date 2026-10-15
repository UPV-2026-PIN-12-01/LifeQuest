const DAY_NAMES = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']
const MONTH_NAMES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

export function startOfWeek(date) {
  const start = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const daysSinceMonday = (start.getDay() + 6) % 7
  start.setDate(start.getDate() - daysSinceMonday)
  return start
}

export function addDays(date, days) {
  const next = new Date(date)
  next.setDate(next.getDate() + days)
  return next
}

export function toIsoDate(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

export function weekDays(weekStart) {
  return DAY_NAMES.map((name, index) => {
    const date = addDays(weekStart, index)
    return { name, date, iso: toIsoDate(date) }
  })
}

export function weekRangeLabel(weekStart) {
  const end = addDays(weekStart, 6)
  const startLabel = `${weekStart.getDate()} ${MONTH_NAMES[weekStart.getMonth()]}`
  const endLabel = `${end.getDate()} ${MONTH_NAMES[end.getMonth()]}`
  return `${startLabel} – ${endLabel}`
}
