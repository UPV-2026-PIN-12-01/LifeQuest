import { useEffect, useId, useState } from 'react'
import AccountStep from '../components/signup/AccountStep.jsx'
import { validateAccount, validatePhoto } from '../utils/validation.js'
import '../styles/signup.css'

export default function SignupPage() {
  const [tab, setTab] = useState('signup')
  const [step, setStep] = useState(1)
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [photoFile, setPhotoFile] = useState(null)
  const [photoPreview, setPhotoPreview] = useState('')
  const [errors, setErrors] = useState({})

  useEffect(() => {
    return () => {
      if (photoPreview) URL.revokeObjectURL(photoPreview)
    }
  }, [photoPreview])

  function showLogin() {
    setTab('login')
  }

  function showSignup() {
    setTab('signup')
    setStep(1)
  }

  function handleFieldChange(field, value) {
    if (field === 'username') setUsername(value)
    if (field === 'email') setEmail(value)
    if (field === 'password') setPassword(value)
    setErrors((current) => {
      if (!current[field]) return current
      const next = { ...current }
      delete next[field]
      return next
    })
  }

  function handlePhotoSelect(file) {
    const photoError = validatePhoto(file)
    if (photoError) {
      setErrors((current) => ({ ...current, photo: photoError }))
      return
    }
    setPhotoFile(file)
    setPhotoPreview(URL.createObjectURL(file))
    setErrors((current) => {
      if (!current.photo) return current
      const next = { ...current }
      delete next.photo
      return next
    })
  }

  function handleAccountNext() {
    const nextErrors = validateAccount({ username, email, password })
    if (Object.keys(nextErrors).length > 0) {
      setErrors(nextErrors)
      return
    }
    setErrors({})
    setStep(2)
  }

  return (
    <div className="signup-page">
      <section className="signup-marketing">
        <div className="signup-brand">
          <span className="signup-brand-mark" aria-hidden="true">
            <SwordIcon />
          </span>
          LifeQuest
        </div>
        <h1>
          Convierte tus
          <br />
          <span className="signup-headline-accent">objetivos</span> en progreso
        </h1>
        <p className="signup-lead">
          Organiza tu vida, completa tareas y evoluciona tu personaje. La
          productividad nunca había sido tan épica.
        </p>
        <ul className="signup-perks">
          <li>
            <StarIcon />
            Gana XP
          </li>
          <li>
            <BagIcon />
            Consigue Oro
          </li>
          <li>
            <TrophyIcon />
            Sube de Nivel
          </li>
        </ul>
      </section>

      <section className="signup-card">
        <div className="signup-tabs" role="tablist" aria-label="Acceso">
          <button
            type="button"
            role="tab"
            aria-selected={tab === 'login'}
            className={tab === 'login' ? 'is-active' : undefined}
            onClick={showLogin}
          >
            Iniciar sesión
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={tab === 'signup'}
            className={tab === 'signup' ? 'is-active' : undefined}
            onClick={showSignup}
          >
            Crear cuenta
          </button>
        </div>

        <div className="signup-panels">
          <div
            className={tab === 'login' ? undefined : 'signup-panel-inactive'}
            aria-hidden={tab !== 'login'}
            inert={tab !== 'login'}
          >
            <LoginStub />
          </div>
          <div
            className={tab === 'signup' ? undefined : 'signup-panel-inactive'}
            aria-hidden={tab !== 'signup'}
            inert={tab !== 'signup'}
          >
            <ol className="signup-stepper" aria-label="Pasos del registro">
              <li className={step === 1 ? 'is-current' : 'is-done'}>
                {step === 2 ? (
                  <button
                    type="button"
                    className="signup-step-back"
                    onClick={() => setStep(1)}
                  >
                    <span className="signup-step-index" aria-hidden="true">
                      ✓
                    </span>
                    Cuenta
                  </button>
                ) : (
                  <>
                    <span className="signup-step-index" aria-hidden="true">
                      1
                    </span>
                    Cuenta
                  </>
                )}
              </li>
              <li className={step === 2 ? 'is-current' : undefined}>
                <span className="signup-step-index" aria-hidden="true">
                  2
                </span>
                Personaje
              </li>
            </ol>

            <div className="signup-step-panels">
              <div
                className={step === 1 ? undefined : 'signup-panel-inactive'}
                aria-hidden={step !== 1}
                inert={step !== 1}
              >
                <AccountStep
                  username={username}
                  email={email}
                  password={password}
                  photoPreview={photoPreview}
                  photoName={photoFile?.name}
                  errors={errors}
                  onFieldChange={handleFieldChange}
                  onPhotoSelect={handlePhotoSelect}
                  onSubmit={handleAccountNext}
                />
              </div>
              <div
                className={step === 2 ? undefined : 'signup-panel-inactive'}
                aria-hidden={step !== 2}
                inert={step !== 2}
              >
                <div className="signup-character-placeholder">
                  <h2>Crea tu personaje</h2>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  )
}

function LoginStub() {
  const emailId = useId()
  const passwordId = useId()

  function handleSubmit(event) {
    event.preventDefault()
  }

  return (
    <form className="signup-login-stub" onSubmit={handleSubmit}>
      <header className="signup-card-header">
        <h2>Iniciar sesión</h2>
        <p>Próximamente</p>
      </header>
      <div className="signup-field">
        <label className="signup-label" htmlFor={emailId}>
          Email
        </label>
        <input
          id={emailId}
          name="login-email"
          type="email"
          autoComplete="username"
          disabled
        />
      </div>
      <div className="signup-field">
        <label className="signup-label" htmlFor={passwordId}>
          Contraseña
        </label>
        <input
          id={passwordId}
          name="login-password"
          type="password"
          autoComplete="current-password"
          disabled
        />
      </div>
      <button className="signup-primary" type="submit" disabled>
        Iniciar sesión
      </button>
    </form>
  )
}

function SwordIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
      <path
        fill="currentColor"
        d="M13.5 2.1 20 8.6l-1.4 1.4-1.8-1.8-8.2 8.2 2.1 2.1-1.4 1.4-2.1-2.1-1.6 1.6H4v-1.6l1.6-1.6-2.1-2.1 1.4-1.4 2.1 2.1 8.2-8.2-1.8-1.8L13.5 2.1z"
      />
    </svg>
  )
}

function StarIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
      <path
        fill="#F5C542"
        d="m12 3 2.5 6.1L21 10l-4.5 4.1L17.8 21 12 17.6 6.2 21l1.3-6.9L3 10l6.5-.9L12 3z"
      />
    </svg>
  )
}

function BagIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
      <path
        fill="#E8B84A"
        d="M8 8h8l1.5 11h-11L8 8zm2.2-3.2c.4-1.3 1.2-2.3 1.8-2.3s1.4 1 1.8 2.3L15 7h-6l1.2-2.2z"
      />
      <path fill="#C9922A" d="M10.5 10.2h3v1.6h-3z" />
    </svg>
  )
}

function TrophyIcon() {
  return (
    <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
      <path
        fill="#E8B84A"
        d="M7 4h10v3a5 5 0 0 1-4 4.9V14h2v2H9v-2h2v-2.1A5 5 0 0 1 7 7V4zm-3 1h3v2H5a1 1 0 0 1-1-1V5zm13 0h3v2a1 1 0 0 1-1 1h-2V5zM8 19h8v2H8z"
      />
    </svg>
  )
}
