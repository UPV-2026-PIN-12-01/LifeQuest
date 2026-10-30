import { useEffect, useRef, useState } from 'react'
import { createChallenge } from '../../services/challenges.js'

const CONDITIONS = [
  {
    name: 'Tiempo riguroso',
    description: 'Completa la tarea respetando estrictamente el tiempo establecido.',
    difficulty: 'EASY',
    reward: [25, 10],
  },
  {
    name: 'No pedir ayuda',
    description: 'Resuelve la actividad sin solicitar ayuda a nadie.',
    difficulty: 'NORMAL',
    reward: [40, 15],
  },
  {
    name: 'No mirar el móvil',
    description: 'No consultar el móvil durante toda la sesión.',
    difficulty: 'HARD',
    reward: [75, 25],
  },
  {
    name: 'Sin interrupciones',
    description: 'Completa la actividad sin ninguna interrupción externa.',
    difficulty: 'HARD',
    reward: [75, 25],
  },
  {
    name: 'No usar solucionario',
    description: 'Resolver los ejercicios sin consultar las soluciones.',
    difficulty: 'HARD',
    reward: [75, 25],
  },
  {
    name: 'Utilizar método nuevo',
    description: 'Resolver utilizando un método que no hayas utilizado anteriormente.',
    difficulty: 'VERY HARD',
    reward: [120, 40],
  },
]

const CUSTOM_INDEX = CONDITIONS.length
const DIFFICULTIES = [
  { value: 'EASY', label: 'Fácil', reward: [25, 10] },
  { value: 'NORMAL', label: 'Normal', reward: [40, 15] },
  { value: 'HARD', label: 'Difícil', reward: [75, 25] },
  { value: 'VERY HARD', label: 'Muy difícil', reward: [120, 40] },
]

export default function ChallengeFormModal({ tasks, loading, onClose, onCreated }) {
  const dialogRef = useRef(null)
  const [taskId, setTaskId] = useState(tasks[0]?.id ? String(tasks[0].id) : '')
  const [conditionIndex, setConditionIndex] = useState(0)
  const [customName, setCustomName] = useState('')
  const [customDescription, setCustomDescription] = useState('')
  const [customDifficulty, setCustomDifficulty] = useState('NORMAL')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)
  const selectedTaskId = tasks.some((task) => String(task.id) === taskId)
    ? taskId
    : (tasks[0]?.id ? String(tasks[0].id) : '')
  const condition = conditionIndex === CUSTOM_INDEX
    ? {
      name: customName.trim(),
      description: customDescription.trim(),
      difficulty: customDifficulty,
      reward: DIFFICULTIES.find((difficulty) => difficulty.value === customDifficulty).reward,
    }
    : CONDITIONS[conditionIndex]
  const selectedTask = tasks.find((task) => String(task.id) === selectedTaskId)

  useEffect(() => {
    if (!dialogRef.current?.open) dialogRef.current?.showModal()
  }, [])

  async function handleSubmit(event) {
    event.preventDefault()
    if (!selectedTaskId) {
      setError('Selecciona una tarea programada.')
      return
    }
    if (conditionIndex === CUSTOM_INDEX && (!condition.name || !condition.description)) {
      setError('Escribe un nombre y describe tu condición personalizada.')
      return
    }

    setBusy(true)
    setError(null)
    try {
      await createChallenge({
        name: condition.name,
        description: condition.description,
        difficulty: condition.difficulty,
        taskId: Number(selectedTaskId),
        requirement: condition.description,
      })
      onCreated()
    } catch (requestError) {
      setError(requestError.message === 'Internal Server Error'
        ? 'El servidor no pudo crear el desafío. Reinicia el backend y revisa sus logs.'
        : requestError.message || 'No se pudo crear el desafío.')
      setBusy(false)
    }
  }

  return (
    <dialog
      ref={dialogRef}
      className="challenge-modal"
      aria-labelledby="challenge-modal-title"
      onClose={onClose}
      onClick={(event) => {
        if (event.target === dialogRef.current) dialogRef.current.close()
      }}
    >
      <form className="challenge-form" onSubmit={handleSubmit} noValidate>
        <header className="challenge-form-header">
          <div>
            <h2 id="challenge-modal-title">Crear desafío</h2>
            <p>Desafíos de hoy: 1 / 3</p>
          </div>
          <button type="button" className="challenge-form-close" onClick={() => dialogRef.current.close()} aria-label="Cerrar">
            ×
          </button>
        </header>

        {loading ? (
          <p className="challenge-form-empty">Cargando tus tareas programadas…</p>
        ) : tasks.length === 0 ? (
          <p className="challenge-form-empty">
            No tienes tareas programadas esta semana. Crea una tarea antes de añadir un desafío.
          </p>
        ) : (
          <>
            <label className="challenge-field">
              <span>Tarea</span>
              <select value={selectedTaskId} onChange={(event) => setTaskId(event.target.value)} required>
                {tasks.map((task) => (
                  <option key={task.id} value={task.id}>
                    {task.icon} {task.title} [{task.status === 'completed' ? 'Completada' : 'Programada'}]
                  </option>
                ))}
              </select>
              {selectedTask && (
                <small>{selectedTask.startTime}–{selectedTask.endTime} · {selectedTask.date}</small>
              )}
            </label>

            <fieldset className="challenge-field">
              <legend>Condición del desafío</legend>
              <div className="challenge-condition-list">
                {CONDITIONS.map((option, index) => (
                  <label key={option.name} className={`challenge-condition${conditionIndex === index ? ' is-selected' : ''}`}>
                    <input
                      type="radio"
                      name="challenge-condition"
                      checked={conditionIndex === index}
                      onChange={() => setConditionIndex(index)}
                    />
                    <span>
                      <strong>{option.name}</strong>
                      <small>{option.description}</small>
                    </span>
                    <em>{difficultyLabel(option.difficulty)}</em>
                  </label>
                ))}
                <label className={`challenge-condition challenge-custom-condition${conditionIndex === CUSTOM_INDEX ? ' is-selected' : ''}`}>
                  <input
                    type="radio"
                    name="challenge-condition"
                    checked={conditionIndex === CUSTOM_INDEX}
                    onChange={() => setConditionIndex(CUSTOM_INDEX)}
                  />
                  <span><strong>Condición personalizada</strong></span>
                </label>
              </div>
            </fieldset>

            {conditionIndex === CUSTOM_INDEX && (
              <div className="challenge-custom-fields">
                <label className="challenge-field">
                  <span>Nombre de la condición</span>
                  <input
                    type="text"
                    value={customName}
                    maxLength={120}
                    onChange={(event) => setCustomName(event.target.value)}
                    placeholder="Ej. Trabajar sin interrupciones"
                  />
                </label>
                <label className="challenge-field">
                  <span>Describe la condición</span>
                  <textarea
                    rows={3}
                    value={customDescription}
                    onChange={(event) => setCustomDescription(event.target.value)}
                    placeholder="Describe qué debes cumplir..."
                  />
                </label>
                <fieldset className="challenge-field">
                  <legend>Dificultad de tu condición</legend>
                  <div className="challenge-difficulty-grid">
                    {DIFFICULTIES.map((difficulty) => (
                      <label key={difficulty.value} className={`challenge-difficulty-choice${customDifficulty === difficulty.value ? ' is-selected' : ''}`}>
                        <input
                          type="radio"
                          name="custom-difficulty"
                          checked={customDifficulty === difficulty.value}
                          onChange={() => setCustomDifficulty(difficulty.value)}
                        />
                        <span>{difficultyIcon(difficulty.value)} <strong>{difficulty.label}</strong><small>+{difficulty.reward[0]} XP</small></span>
                      </label>
                    ))}
                  </div>
                </fieldset>
              </div>
            )}

            <div className="challenge-reward-preview">
              <span>{difficultyIcon(condition.difficulty)} <strong>{difficultyLabel(condition.difficulty)}</strong></span>
              <span>+{condition.reward[0]} XP · +{condition.reward[1]} 🪙</span>
            </div>
          </>
        )}

        {error && <p className="challenge-form-error" role="alert">{error}</p>}

        <footer className="challenge-form-actions">
          <button type="button" className="challenge-form-cancel" onClick={() => dialogRef.current.close()} disabled={busy}>
            Cancelar
          </button>
          <button type="submit" className="challenge-form-submit" disabled={busy || tasks.length === 0}>
            {busy ? 'Creando…' : 'Crear desafío'}
          </button>
        </footer>
      </form>
    </dialog>
  )
}

function difficultyLabel(difficulty) {
  return {
    EASY: 'Fácil',
    NORMAL: 'Normal',
    HARD: 'Difícil',
    'VERY HARD': 'Muy difícil',
  }[difficulty] || difficulty
}

function difficultyIcon(difficulty) {
  return { EASY: '🟢', NORMAL: '🟡', HARD: '🟠', 'VERY HARD': '🔴' }[difficulty] || '🟣'
}
