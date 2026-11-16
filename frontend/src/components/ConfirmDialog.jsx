import { useEffect, useRef } from 'react'
import '../styles/challenges.css'
import '../styles/confirm-dialog.css'

// Modal "¿Seguro?" en el estilo del juego. Esc o un clic fuera cancelan, salvo mientras busy.
export default function ConfirmDialog({
  title,
  message,
  confirmLabel = 'Confirmar',
  busyLabel = 'Procesando…',
  cancelLabel = 'Cancelar',
  danger = false,
  busy = false,
  error = null,
  onConfirm,
  onCancel,
}) {
  const dialogRef = useRef(null)

  useEffect(() => {
    if (!dialogRef.current?.open) dialogRef.current?.showModal()
  }, [])

  function cancel() {
    if (!busy) onCancel()
  }

  return (
    <dialog
      ref={dialogRef}
      className="challenge-modal confirm-dialog"
      aria-labelledby="confirm-dialog-title"
      aria-describedby="confirm-dialog-message"
      onCancel={(event) => {
        event.preventDefault()
        cancel()
      }}
      onClick={(event) => {
        if (event.target === dialogRef.current) cancel()
      }}
    >
      <div className="confirm-dialog-body">
        <h2 id="confirm-dialog-title">{title}</h2>
        <p id="confirm-dialog-message">{message}</p>
        {error && <p className="challenges-error" role="alert">{error}</p>}
      </div>
      <div className="confirm-dialog-actions">
        <button type="button" className="confirm-dialog-button" onClick={cancel} disabled={busy} autoFocus>
          {cancelLabel}
        </button>
        <button
          type="button"
          className={`confirm-dialog-button is-primary${danger ? ' is-danger' : ''}`}
          onClick={onConfirm}
          disabled={busy}
        >
          {busy ? busyLabel : confirmLabel}
        </button>
      </div>
    </dialog>
  )
}
