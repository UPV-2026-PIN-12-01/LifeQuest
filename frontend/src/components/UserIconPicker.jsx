import { CLASS_ICONS } from '../utils/playerClasses.js'

export default function UserIconPicker({
  value,
  onChange,
  label = 'Icono del perfil',
  disabled = false,
}) {
  return (
    <fieldset className="user-icon-picker">
      <legend>{label}</legend>
      <div className="user-icon-options">
        {CLASS_ICONS.map((icon) => (
          <button
            key={icon}
            type="button"
            aria-pressed={value === icon}
            disabled={disabled}
            aria-label={`Elegir icono ${icon}`}
            className={`user-icon-option${value === icon ? ' is-selected' : ''}`}
            onClick={() => onChange(icon)}
          >
            {icon}
          </button>
        ))}
      </div>
    </fieldset>
  )
}
