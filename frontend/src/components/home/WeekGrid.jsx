import { toIsoDate, weekDays } from '../../utils/week.js'
import DayColumn from './DayColumn.jsx'

export default function WeekGrid({ weekStart, tasks, onSelectTask }) {
  const todayIso = toIsoDate(new Date())

  return (
    <div className="week-grid">
      {weekDays(weekStart).map((day) => (
        <DayColumn
          key={day.iso}
          day={day}
          isToday={day.iso === todayIso}
          onSelectTask={onSelectTask}
          tasks={tasks
            .filter((task) => task.date === day.iso)
            .sort((a, b) => a.startTime.localeCompare(b.startTime))}
        />
      ))}
    </div>
  )
}
