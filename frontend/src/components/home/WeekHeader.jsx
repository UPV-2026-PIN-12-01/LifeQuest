export default function WeekHeader({ label, onPrevious, onNext, onAddTask }) {
  return (
    <header className="week-header">
      <div className="week-nav">
        <button type="button" className="week-nav-button" onClick={onPrevious} aria-label="Semana anterior">
          ‹
        </button>
        <h1 className="week-title">{label}</h1>
        <button type="button" className="week-nav-button" onClick={onNext} aria-label="Semana siguiente">
          ›
        </button>
      </div>
      {/* Disabled until the create-task form exists. */}
      <button
        type="button"
        className="add-task-button"
        onClick={onAddTask}
        disabled={!onAddTask}
        title={onAddTask ? undefined : 'Próximamente'}
      >
        <span aria-hidden="true">+</span> Añadir tarea
      </button>
    </header>
  )
}
