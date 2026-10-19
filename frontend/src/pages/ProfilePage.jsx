import { useEffect, useId, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getJson, patchForm, patchJson } from '../services/http.js'
import { classById } from '../utils/playerClasses.js'
import { validatePhoto, validateProfile } from '../utils/validation.js'
import '../styles/profile.css'

const LATER_ROWS = ['Notificaciones', 'Privacidad', 'Tema visual']

export default function ProfilePage() {
  const navigate = useNavigate()
  const photoId = useId()
  const nameId = useId()
  const emailId = useId()
  const passwordId = useId()
  const confirmId = useId()
  const passwordHintId = useId()
  const previewRef = useRef('')

  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [name, setName] = useState('')
  const [accountName, setAccountName] = useState('')
  const [playerClass, setPlayerClass] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [profileUrl, setProfileUrl] = useState('')
  const [photoFile, setPhotoFile] = useState(null)
  const [photoPreview, setPhotoPreview] = useState('')
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
      .then((account) => {
        if (cancelled) return
        const user = account?.user
        if (!user) {
          setLoadError({ status: 404 })
          return
        }
        setName(user.playerName ?? '')
        setAccountName(user.username ?? '')
        setPlayerClass(user.playerClass ?? '')
        setEmail(user.email ?? '')
        setProfileUrl(user.profileUrl ?? '')
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
    setPhotoFile(file)
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
        document.querySelector('.settings-form [aria-invalid="true"]')?.focus()
      })
      return
    }
    const body = {
      playerName: name.trim(),
      email: email.trim().toLowerCase(),
    }
    if (password) body.password = password
    setSaving(true)
    setSaved(false)
    setFormError(null)
    try {
      const payload = await patchJson('/api/users/me', body)
      setName(payload.user?.playerName ?? body.playerName)
      setEmail(payload.user?.email ?? body.email)
      setProfileUrl(payload.user?.profileUrl ?? '')
      setPassword('')
      setConfirmPassword('')
      if (photoFile) {
        const formData = new FormData()
        formData.append('photo', photoFile)
        try {
          const photoPayload = await patchForm('/api/users/me/photo', formData)
          setProfileUrl(photoPayload.user?.profileUrl ?? '')
          setPhotoFile(null)
          if (previewRef.current) URL.revokeObjectURL(previewRef.current)
          previewRef.current = ''
          setPhotoPreview('')
        } catch (error) {
          if (error.status === 401) {
            navigate('/login', { replace: true })
            return
          }
          setErrors({ photo: photoErrorFromSave(error) })
          setSaved(false)
          requestAnimationFrame(() => {
            document.querySelector('.settings-form [aria-invalid="true"]')?.focus()
          })
          return
        }
      }
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
          document.querySelector('.settings-form [aria-invalid="true"]')?.focus()
        })
      } else {
        setFormError(saveErrorMessage(error))
      }
    } finally {
      setSaving(false)
    }
  }

  const classIcon = classById(playerClass).icon
  const faceUrl = photoPreview || profileUrl

  return (
    <main className="profile-page">
      <div className="profile-wrap">
        <h1 className="settings-title">Ajustes</h1>

        {loading ? (
          <section className="profile-card" aria-busy="true">
            <p className="profile-hint">Cargando…</p>
          </section>
        ) : loadError ? (
          <section className="profile-card">
            <p className="profile-error" role="alert">
              {loadErrorMessage(loadError)}
            </p>
          </section>
        ) : (
          <>
            <form className="settings-form" onSubmit={handleSubmit} noValidate>
              <section className="settings-hero">
                <h2>Personaje</h2>
                <div className="settings-hero-body">
                  <div className="profile-photo">
                    <div className="profile-photo-face">
                      {faceUrl ? (
                        <img src={faceUrl} alt="Foto del personaje" />
                      ) : (
                        <span className="profile-photo-emoji" aria-hidden="true">
                          {classIcon}
                        </span>
                      )}
                    </div>
                    <label className="settings-pencil" htmlFor={photoId}>
                      <span className="visually-hidden">Cambiar foto</span>
                      ✎
                    </label>
                  </div>
                  <div className="profile-field settings-name">
                    <label className="profile-label" htmlFor={nameId}>
                      Nombre del personaje
                    </label>
                    <input
                      id={nameId}
                      name="playerName"
                      type="text"
                      autoComplete="nickname"
                      value={name}
                      onChange={(event) => {
                        setName(event.target.value)
                        forgetSaved()
                        clearError('name')
                      }}
                      aria-invalid={Boolean(errors.name)}
                      aria-describedby={errors.name ? `${nameId}-error` : undefined}
                    />
                    {playerClass && <p className="settings-class">{playerClass}</p>}
                    {errors.name && (
                      <p className="profile-error" id={`${nameId}-error`} role="alert">
                        {errors.name}
                      </p>
                    )}
                  </div>
                </div>
                <input
                  id={photoId}
                  className="profile-file"
                  type="file"
                  accept="image/jpeg,image/png,image/webp,image/gif,.jpg,.jpeg,.png,.webp,.gif"
                  onChange={handlePhotoChange}
                  aria-invalid={Boolean(errors.photo)}
                  aria-describedby={errors.photo ? `${photoId}-error` : undefined}
                />
                {errors.photo && (
                  <p className="profile-error" id={`${photoId}-error`} role="alert">
                    {errors.photo}
                  </p>
                )}
              </section>

              <section className="profile-card settings-account">
                <header className="settings-account-head">
                  <h2>Cuenta</h2>
                  <p className="settings-static">{accountName}</p>
                </header>
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
              </section>
            </form>

            <ul className="settings-rows">
              {LATER_ROWS.map((label) => (
                <li key={label}>
                  <span>{label}</span>
                  <span className="settings-soon">Pronto</span>
                </li>
              ))}
            </ul>
          </>
        )}
      </div>
    </main>
  )
}

function photoErrorFromSave(error) {
  if (error.message === 'Photo must be jpeg, png, webp, or gif and 2MB or smaller') {
    return 'La foto debe ser jpeg, png, webp o gif y pesar 2 MB o menos'
  }
  if (error.message === 'Photo is required' || error.message === 'Invalid photo') {
    return 'La foto no es válida'
  }
  return 'No se pudo guardar la foto.'
}

function fieldErrorsFromSave(error) {
  if (error.message === 'Player name already exists') {
    return { name: 'El nombre del personaje ya existe' }
  }
  if (error.message === 'Player name is required') return { name: 'El nombre es obligatorio' }
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
