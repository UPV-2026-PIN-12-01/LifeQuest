import { useId } from 'react'
import FieldError from './FieldError.jsx'
import { PLAYER_CLASSES } from '../../utils/playerClasses.js'

export default function CharacterStep({
  playerClass,
  playerName,
  error,
  formError,
  submitting,
  onClassChange,
  onPlayerNameChange,
  onBack,
  onSubmit,
}) {
  const nameId = useId()

  function handleSubmit(event) {
    event.preventDefault()
    onSubmit()
  }

  return (
    <form className="signup-character" onSubmit={handleSubmit} noValidate>
      <div className="signup-character-pane">
        <header className="signup-card-header">
          <h2>Crea tu personaje</h2>
          <p>Elige tu clase y dale nombre a tu héroe</p>
        </header>

        <div className="signup-field signup-class-field">
        <span className="signup-label" id="signup-class-label">
          Clase
        </span>
        <div
          className="signup-class-list"
          role="radiogroup"
          aria-labelledby="signup-class-label"
        >
          {PLAYER_CLASSES.map((item) => {
            const selectedClass = item.id === playerClass
            return (
              <button
                key={item.id}
                type="button"
                role="radio"
                aria-checked={selectedClass}
                className={[
                  'signup-class-card',
                  `is-${item.id.toLowerCase()}`,
                  selectedClass ? 'is-selected' : '',
                ]
                  .filter(Boolean)
                  .join(' ')}
                onClick={() => onClassChange(item.id)}
              >
                <img
                  className="signup-class-portrait"
                  src={item.image}
                  alt=""
                />
                <span className="signup-class-copy">
                  <span className="signup-class-title">
                    {item.name}
                    {selectedClass && (
                      <span className="signup-class-badge">Seleccionado</span>
                    )}
                  </span>
                  <span className="signup-class-description" title={item.description}>
                    {item.description}
                  </span>
                  <span className="signup-class-stats">{item.stats}</span>
                </span>
              </button>
            )
          })}
        </div>
      </div>

      <div className="signup-field">
        <label className="signup-label" htmlFor={nameId}>
          Nombre del personaje
        </label>
        <div className="signup-control">
          <input
            id={nameId}
            name="playerName"
            type="text"
            placeholder="Ej: Alejandro el Valiente"
            value={playerName}
            onChange={(event) => onPlayerNameChange(event.target.value)}
            aria-invalid={Boolean(error)}
            aria-describedby={`${nameId}-hint${error ? ` ${nameId}-error` : ''}`}
          />
          <FieldError id={`${nameId}-error`}>{error}</FieldError>
        </div>
        <p className="signup-hint" id={`${nameId}-hint`}>
          Este es el nombre de tu héroe dentro del juego.
        </p>
      </div>
      </div>

      {formError && (
        <p className="signup-form-error" role="alert">
          {formError}
        </p>
      )}

      <div className="signup-actions">
        <button
          className="signup-secondary"
          type="button"
          onClick={onBack}
          disabled={submitting}
        >
          ← Volver
        </button>
        <button className="signup-primary" type="submit" disabled={submitting}>
          {submitting ? 'Creando…' : '✨ Empezar aventura'}
        </button>
      </div>
    </form>
  )
}
