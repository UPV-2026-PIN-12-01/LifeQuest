import { useEffect, useId, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getJson, patchJson } from '../services/http.js'
import { CLASS_ICONS } from '../utils/playerClasses.js'
import { validatePhoto, validateProfile } from '../utils/validation.js'
import '../styles/profile.css'

export default function ProfilePage() {
  const navigate = useNavigate()
  const photoId = useId()
  const nameId = useId()
  const emailId = useId()
  const passwordId = useId()
  const confirmId = useId()
  const passwordHintId = useId()
  const iconLabelId = useId()
  const previewRef = useRef('')

  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [photoPreview, setPhotoPreview] = useState('')
  const [icon, setIcon] = useState('')
  const [errors, setErrors] = useState({})
  const [formError, setFormError] = useState(null)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    if (loadError?.status === 401) navigate('/login', { replace: true })
  }, [loadError, navigate])

  useEffect(() => {
    let cancelled = false
    getJson('/api/users/me')
      .then((payload) => {
        if (cancelled) return
        const user = payload?.user
        if (!user) {
          setLoadError({ status: 404 })
          return
        }
        setName(user.username ?? '')
        setEmail(user.email ?? '')
        setIcon(user.userIcon ?? '')
      })
      .catch((error) => {
        if (!cancelled) setLoadError(error)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    return () => {
      if (previewRef.current) URL.revokeObjectURL(previewRef.current)
    }
  }, [])

  function forgetSaved() {
    setSaved(false)
    setFormError(null)
  }

  function clearError(field) {
    setErrors((current) => {
      if (!current[field]) return current
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  function handlePhotoChange(event) {
    const file = event.target.files?.[0]
    event.target.value = ''
    if (!file) return
    const photoError = validatePhoto(file)
    forgetSaved()
    if (photoError) {
      setErrors((current) => ({ ...current, photo: photoError }))
      return
    }
    const nextUrl = URL.createObjectURL(file)
    if (previewRef.current) URL.revokeObjectURL(previewRef.current)
    previewRef.current = nextUrl
    setPhotoPreview(nextUrl)
    clearError('photo')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const nextErrors = validateProfile({ name, email, password, confirmPassword })
    if (errors.photo) nextErrors.photo = errors.photo
    if (Object.keys(nextErrors).length > 0) {
      setErrors(nextErrors)
      setFormError(null)
      setSaved(false)
      requestAnimationFrame(() => {
        document.querySelector('.profile-card [aria-invalid="true"]')?.focus()
      })
      return
    }
    const body = {
      username: name.trim(),
      email: email.trim().toLowerCase(),
      userIcon: icon,
    }
    if (password) body.password = password
    setSaving(true)
    setSaved(false)
    setFormError(null)
    try {
      const payload = await patchJson('/api/users/me', body)
      const user = payload.user
      setName(user.username ?? '')
      setEmail(user.email ?? '')
      setIcon(user.userIcon ?? '')
      setPassword('')
      setConfirmPassword('')
      setErrors({})
      setSaved(true)
    } catch (error) {
      if (error.status === 401) {
        navigate('/login', { replace: true })
        return
      }
      const fieldErrors = fieldErrorsFromSave(error)
      if (fieldErrors) {
        setErrors(fieldErrors)
        requestAnimationFrame(() => {
          document.querySelector('.profile-card [aria-invalid="true"]')?.focus()
        })
      } else {
        setFormError(saveErrorMessage(error))
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="profile-page">
      <div className="profile-wrap">
        <Link className="profile-back" to="/home">
          ← Inicio
        </Link>

        {loading ? (
          <section className="profile-card" aria-busy="true">
            <h1>Tu perfil</h1>
            <p className="profile-hint">Cargando tu perfil…</p>
          </section>
        ) : loadError ? (
          <section className="profile-card">
            <h1>Tu perfil</h1>
            <p className="profile-error" role="alert">
              {loadErrorMessage(loadError)}
            </p>
          </section>
        ) : (
          <form className="profile-card" onSubmit={handleSubmit} noValidate>
            <h1>Tu perfil</h1>

            <div className="profile-photo-block">
              <div className="profile-photo-row">
                <div className="profile-photo">
                  <div className="profile-photo-face">
                    {photoPreview ? (
                      <img src={photoPreview} alt="Foto de perfil" />
                    ) : (
                      <span className="profile-photo-emoji" aria-hidden="true">
                        {icon}
                      </span>
                    )}
                  </div>
                  {photoPreview && icon && (
                    <span className="profile-photo-badge" aria-hidden="true">
                      {icon}
                    </span>
                  )}
                </div>
                <div className="profile-photo-copy">
                  <span className="profile-photo-title" id={`${photoId}-label`}>
                    Foto de perfil
                  </span>
                  <label className="profile-upload" htmlFor={photoId}>
                    Subir imagen
                  </label>
                </div>
              </div>
              <input
                id={photoId}
                className="profile-file"
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif,.jpg,.jpeg,.png,.webp,.gif"
                onChange={handlePhotoChange}
                aria-labelledby={`${photoId}-label`}
                aria-invalid={Boolean(errors.photo)}
                aria-describedby={errors.photo ? `${photoId}-error` : undefined}
              />
              {errors.photo && (
                <p className="profile-error" id={`${photoId}-error`} role="alert">
                  {errors.photo}
                </p>
              )}
            </div>

            <div className="profile-field">
              <span className="profile-label" id={iconLabelId}>
                O elige un emoji
              </span>
              <div
                className="profile-emoji-grid"
                role="listbox"
                aria-labelledby={iconLabelId}
              >
                {CLASS_ICONS.map((emoji) => (
                  <button
                    key={emoji}
                    type="button"
                    role="option"
                    aria-selected={icon === emoji}
                    className={icon === emoji ? 'profile-emoji is-selected' : 'profile-emoji'}
                    onClick={() => {
                      setIcon(emoji)
                      forgetSaved()
                    }}
                  >
                    {emoji}
                  </button>
                ))}
              </div>
            </div>

            <div className="profile-field">
              <label className="profile-label" htmlFor={nameId}>
                Nombre
              </label>
              <input
                id={nameId}
                name="username"
                type="text"
                autoComplete="username"
                value={name}
                onChange={(event) => {
                  setName(event.target.value)
                  forgetSaved()
                  clearError('name')
                }}
                aria-invalid={Boolean(errors.name)}
                aria-describedby={errors.name ? `${nameId}-error` : undefined}
              />
              {errors.name && (
                <p className="profile-error" id={`${nameId}-error`} role="alert">
                  {errors.name}
                </p>
              )}
            </div>

            <div className="profile-field">
              <label className="profile-label" htmlFor={emailId}>
                Correo
              </label>
              <input
                id={emailId}
                name="email"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(event) => {
                  setEmail(event.target.value)
                  forgetSaved()
                  clearError('email')
                }}
                aria-invalid={Boolean(errors.email)}
                aria-describedby={errors.email ? `${emailId}-error` : undefined}
              />
              {errors.email && (
                <p className="profile-error" id={`${emailId}-error`} role="alert">
                  {errors.email}
                </p>
              )}
            </div>

            <div className="profile-field profile-password">
              <span className="profile-label">Contraseña</span>
              <p className="profile-hint" id={passwordHintId}>
                Déjala en blanco si no quieres cambiarla.
              </p>
              <label className="profile-sublabel" htmlFor={passwordId}>
                Nueva contraseña
              </label>
              <input
                id={passwordId}
                name="new-password"
                type="password"
                autoComplete="new-password"
                value={password}
                onChange={(event) => {
                  setPassword(event.target.value)
                  forgetSaved()
                  clearError('password')
                }}
                aria-invalid={Boolean(errors.password)}
                aria-describedby={
                  errors.password ? `${passwordId}-error ${passwordHintId}` : passwordHintId
                }
              />
              {errors.password && (
                <p className="profile-error" id={`${passwordId}-error`} role="alert">
                  {errors.password}
                </p>
              )}
              <label className="profile-sublabel" htmlFor={confirmId}>
                Repetir contraseña
              </label>
              <input
                id={confirmId}
                name="confirm-password"
                type="password"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(event) => {
                  setConfirmPassword(event.target.value)
                  forgetSaved()
                  clearError('confirmPassword')
                }}
                aria-invalid={Boolean(errors.confirmPassword)}
                aria-describedby={
                  errors.confirmPassword ? `${confirmId}-error` : undefined
                }
              />
              {errors.confirmPassword && (
                <p className="profile-error" id={`${confirmId}-error`} role="alert">
                  {errors.confirmPassword}
                </p>
              )}
            </div>

            <button className="profile-save" type="submit" disabled={saving}>
              {saving ? 'Guardando…' : 'Guardar cambios'}
            </button>
            {formError && (
              <p className="profile-error" role="alert">
                {formError}
              </p>
            )}
            {saved && (
              <p className="profile-saved" role="status">
                Cambios guardados.
              </p>
            )}
          </form>
        )}
      </div>
    </main>
  )
}

function fieldErrorsFromSave(error) {
  if (error.message === 'Username already exists') return { name: 'El nombre ya existe' }
  if (error.message === 'Email already exists') return { email: 'El correo ya existe' }
  if (error.message === 'Invalid email' || error.message === 'Email is required') {
    return { email: 'El correo no es válido' }
  }
  if (error.message === 'Password must be at least 6 characters') {
    return { password: 'La contraseña debe tener al menos 6 caracteres' }
  }
  return null
}

function saveErrorMessage(error) {
  if (error.status === 0) return 'No se pudo conectar con el servidor.'
  if (error.status >= 500) return 'No se pudieron guardar los cambios. Inténtalo de nuevo.'
  return 'No se pudieron guardar los cambios.'
}

function loadErrorMessage(error) {
  if (error.status === 401) return 'Tu sesión ha caducado. Vuelve a iniciar sesión.'
  if (error.status === 404) return 'No encontramos tu usuario.'
  if (error.status === 0) return 'No se pudo conectar con el servidor.'
  return 'No se pudo cargar tu perfil. Inténtalo de nuevo más tarde.'
}
