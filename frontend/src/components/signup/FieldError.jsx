export default function FieldError({ id, children }) {
  if (!children) {
    return null
  }

  return (
    <p className="signup-error-tooltip" id={id} role="alert">
      <span className="signup-error-icon" aria-hidden="true">
        !
      </span>
      {children}
    </p>
  )
}
