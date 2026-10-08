import { useId } from 'react'

export default function AccountStep({
  username,
  email,
  password,
  photoPreview,
  photoName,
  errors,
  onFieldChange,
  onPhotoSelect,
  onSubmit,
}) {
  const usernameId = useId()
  const emailId = useId()
  const passwordId = useId()
  const photoId = useId()

  function handleSubmit(event) {
    event.preventDefault()
    onSubmit()
  }

  function handlePhotoChange(event) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (file) {
      onPhotoSelect(file)
    }
  }

  return (
    <form className="signup-account" onSubmit={handleSubmit} noValidate>
      <header className="signup-card-header">
        <h2>Crea tu cuenta</h2>
        <p>Información de acceso y perfil</p>
      </header>

      <div className="signup-field">
        <span className="signup-label" id={`${photoId}-label`}>
          Foto de perfil (opcional)
        </span>
        <div className="signup-photo-row">
          <div
            className="signup-photo-preview"
            aria-hidden="true"
            data-photo-name={photoName || undefined}
          >
            {photoPreview ? (
              <img src={photoPreview} alt="" />
            ) : (
              <UserSilhouette />
            )}
          </div>
          <label className="signup-photo-upload" htmlFor={photoId}>
            <CameraIcon />
            Subir foto de perfil
          </label>
          <input
            id={photoId}
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif,.jpg,.jpeg,.png,.webp,.gif"
            onChange={handlePhotoChange}
            aria-labelledby={`${photoId}-label`}
            aria-invalid={Boolean(errors.photo)}
            aria-describedby={errors.photo ? `${photoId}-error` : undefined}
          />
        </div>
        {errors.photo && (
          <p className="signup-error" id={`${photoId}-error`}>
            {errors.photo}
          </p>
        )}
      </div>

      <div className="signup-field">
        <label className="signup-label" htmlFor={usernameId}>
          Nombre de usuario
        </label>
        <input
          id={usernameId}
          name="username"
          type="text"
          autoComplete="username"
          value={username}
          onChange={(event) => onFieldChange('username', event.target.value)}
          aria-invalid={Boolean(errors.username)}
          aria-describedby={`${usernameId}-hint${errors.username ? ` ${usernameId}-error` : ''}`}
        />
        <p className="signup-hint" id={`${usernameId}-hint`}>
          Este será tu nombre de cuenta visible para otros.
        </p>
        {errors.username && (
          <p className="signup-error" id={`${usernameId}-error`}>
            {errors.username}
          </p>
        )}
      </div>

      <div className="signup-field">
        <label className="signup-label" htmlFor={emailId}>
          Email
        </label>
        <input
          id={emailId}
          name="email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => onFieldChange('email', event.target.value)}
          aria-invalid={Boolean(errors.email)}
          aria-describedby={errors.email ? `${emailId}-error` : undefined}
        />
        {errors.email && (
          <p className="signup-error" id={`${emailId}-error`}>
            {errors.email}
          </p>
        )}
      </div>

      <div className="signup-field">
        <label className="signup-label" htmlFor={passwordId}>
          Contraseña
        </label>
        <input
          id={passwordId}
          name="password"
          type="password"
          autoComplete="new-password"
          value={password}
          onChange={(event) => onFieldChange('password', event.target.value)}
          aria-invalid={Boolean(errors.password)}
          aria-describedby={errors.password ? `${passwordId}-error` : undefined}
        />
        {errors.password && (
          <p className="signup-error" id={`${passwordId}-error`}>
            {errors.password}
          </p>
        )}
      </div>

      <button className="signup-primary" type="submit">
        Siguiente → Crear personaje
      </button>
    </form>
  )
}

function UserSilhouette() {
  return (
    <svg viewBox="0 0 24 24" width="28" height="28" aria-hidden="true">
      <circle cx="12" cy="8" r="4" fill="currentColor" />
      <path
        d="M4 20c1.6-4 4.4-6 8-6s6.4 2 8 6"
        fill="none"
        stroke="currentColor"
        strokeLinecap="round"
        strokeWidth="2"
      />
    </svg>
  )
}

function CameraIcon() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
      <path
        fill="currentColor"
        d="M9 4h6l1.2 2H20a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h3.8L9 4zm3 13a4 4 0 1 0 0-8 4 4 0 0 0 0 8z"
      />
    </svg>
  )
}
