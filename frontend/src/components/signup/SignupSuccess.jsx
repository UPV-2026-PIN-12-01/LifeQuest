import { useMemo } from 'react'

const COLORS = ['#f472b6', '#fbbf24', '#34d399', '#60a5fa', '#c4b5fd', '#fb7185']

// TODO remove once dashboard done
export const SIGNUP_SUCCESS_TODO = 'TODO remove once dashboard done'

export default function SignupSuccess({ onDismiss }) {
  const pieces = useMemo(
    () =>
      Array.from({ length: 42 }, (_, index) => ({
        id: index,
        left: `${(index * 13) % 100}%`,
        delay: `${(index % 12) * 0.1}s`,
        duration: `${2.4 + (index % 6) * 0.2}s`,
        color: COLORS[index % COLORS.length],
      })),
    [],
  )

  return (
    <div
      className="signup-success"
      role="dialog"
      aria-modal="true"
      aria-labelledby="signup-success-title"
      onClick={(event) => {
        if (event.target === event.currentTarget) onDismiss()
      }}
    >
      <div className="signup-confetti" aria-hidden="true">
        {pieces.map((piece) => (
          <span
            key={piece.id}
            style={{
              left: piece.left,
              animationDelay: piece.delay,
              animationDuration: piece.duration,
              background: piece.color,
            }}
          />
        ))}
      </div>
      <div className="signup-success-card">
        <h2 id="signup-success-title">¡Aventura empezada!</h2>
        <p>{SIGNUP_SUCCESS_TODO}</p>
      </div>
    </div>
  )
}
