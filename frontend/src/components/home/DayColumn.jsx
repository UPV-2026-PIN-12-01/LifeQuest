import TaskCard from './TaskCard.jsx'

export default function DayColumn({ day, tasks, isToday }) {
  return (
    <section className={`day-column${isToday ? ' is-today' : ''}`} aria-current={isToday ? 'date' : undefined}>
      <header className="day-header">
        <span className="day-name">{day.name}</span>
        <span className="day-number">{day.date.getDate()}</span>
      </header>
      <div className="day-tasks">
        {tasks.length === 0 ? (
          <span className="day-empty">Día libre</span>
        ) : (
          tasks.map((task) => <TaskCard key={task.id} task={task} />)
        )}
      </div>
    </section>
  )
}
