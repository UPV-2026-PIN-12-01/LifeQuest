import { useId } from 'react'
import {
  CLASS_ICONS,
  PLAYER_CLASSES,
  classById,
} from '../../utils/playerClasses.js'

export default function CharacterStep({
  playerClass,
  playerName,
  userIcon,
  error,
  onClassChange,
  onPlayerNameChange,
  onIconChange,
  onBack,
  onSubmit,
}) {
  const nameId = useId()
  const selected = classById(playerClass)
  const usingDefaultIcon = userIcon === selected.icon

  function handleSubmit(event) {
    event.preventDefault()
    onSubmit()
  }

  return (
    <form className="signup-character" onSubmit={handleSubmit} noValidate>
      <header className="signup-card-header">
        <h2>Crea tu personaje</h2>
        <p>Elige tu clase y dale nombre a tu héroe</p>
      </header>

      <div className="signup-field">
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
                    <span aria-hidden="true">{item.icon}</span>
                    {item.name}
                    {selectedClass && (
                      <span className="signup-class-badge">Seleccionado</span>
                    )}
                  </span>
                  <span className="signup-class-description">
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
        <p className="signup-hint" id={`${nameId}-hint`}>
          Este es el nombre de tu héroe dentro del juego.
        </p>
        {error && (
          <p className="signup-error" id={`${nameId}-error`}>
            {error}
          </p>
        )}
      </div>

      <div className="signup-field signup-icon-field">
        <span className="signup-label" id="signup-icon-label">
          Icono del personaje
        </span>
        <div className="signup-icon-preview">
          <span className="signup-icon-current" aria-hidden="true">
            {userIcon}
          </span>
          <span>
            <strong>
              {usingDefaultIcon
                ? `Predeterminado de ${selected.name}`
                : 'Icono personalizado'}
            </strong>
            <span className="signup-hint">
              Puedes cambiar el icono eligiendo un emoji.
            </span>
          </span>
        </div>
        <div
          className="signup-emoji-grid"
          role="listbox"
          aria-labelledby="signup-icon-label"
        >
          {CLASS_ICONS.map((emoji) => (
            <button
              key={emoji}
              type="button"
              role="option"
              aria-selected={userIcon === emoji}
              className={
                userIcon === emoji
                  ? 'signup-emoji is-selected'
                  : 'signup-emoji'
              }
              onClick={() => onIconChange(emoji)}
            >
              {emoji}
            </button>
          ))}
        </div>
      </div>

      <div className="signup-actions">
        <button className="signup-secondary" type="button" onClick={onBack}>
          ← Volver
        </button>
        <button className="signup-primary" type="submit">
          ✨ Empezar aventura
        </button>
      </div>
    </form>
  )
}
