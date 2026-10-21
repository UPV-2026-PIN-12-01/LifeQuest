export default function WeekHeader({ label, rangeLabel, onPrevious, onNext, onAddTask }) {
  return (
    <header className="week-header">
      <div className="week-nav">
        <button type="button" className="week-nav-button" onClick={onPrevious} aria-label="Semana anterior">
          ‹
        </button>
        <button type="button" className="week-nav-button" onClick={onNext} aria-label="Semana siguiente">
          ›
        </button>
        <h1 className="week-title">{label}</h1>
        {rangeLabel && <span className="week-range">{rangeLabel}</span>}
      </div>
      <button
        type="button"
        className="add-task-button"
        onClick={onAddTask}
        disabled={!onAddTask}
        title={onAddTask ? undefined : 'Próximamente'}
      >
        <span aria-hidden="true">+</span> Nueva misión
      </button>
    </header>
  )
}
