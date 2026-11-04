import { useEffect, useState } from "react"
import { getJson, requestJson } from "../services/http.js"

export default function Goals() {
  const [goals, setGoals] = useState([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [updatingGoal, setUpdatingGoal] = useState(null)
  const [error, setError] = useState("")
  const [rating, setRating] = useState({})
  const [evaluating, setEvaluating] = useState(null)
  const [reward, setReward] = useState(null)
  const [showCreate, setShowCreate] = useState(false)
  const [title, setTitle] = useState("")
  const [description, setDescription] = useState("")
  const [startDate, setStartDate] = useState("")
  const [endDate, setEndDate] = useState("")
  const [xpReward, setXpReward] = useState(200)
  const [goldReward, setGoldReward] = useState(75)

  useEffect(() => {
    let cancelled = false
    getJson("/api/players/me/goals")
      .then((loadedGoals) => {
        if (!cancelled) setGoals(loadedGoals)
      })
      .catch((loadError) => {
        if (!cancelled) setError(loadError.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  async function evaluate(goal, status) {
    const goalRating = rating[goal.id]
    if (status === "completed" && !goalRating) {
      setError("Selecciona una valoración antes de completar el objetivo.")
      return
    }
    setUpdatingGoal(goal.id)
    setError("")
    try {
      const updatedGoal = await requestJson(
        `/api/players/me/goals/${goal.id}/evaluate`,
        {
          method: "PATCH",
          body: {
            completed: status === "completed",
            rating: status === "completed" ? goalRating : null,
          },
        },
      )
      setGoals((currentGoals) =>
        currentGoals.map((currentGoal) =>
          currentGoal.id === updatedGoal.id ? updatedGoal : currentGoal,
        ),
      )
      setEvaluating(null)
      if (status === "completed") {
        setReward({ id: goal.id, xp: goal.xpReward, gold: goal.goldReward })
        window.setTimeout(() => setReward(null), 3000)
      }
    } catch (updateError) {
      setError(updateError.message)
    } finally {
      setUpdatingGoal(null)
    }
  }

  async function createGoal(event) {
    event.preventDefault()
    if (!title.trim() || !startDate || !endDate || endDate < startDate) return
    setSaving(true)
    setError("")
    try {
      const createdGoal = await requestJson("/api/players/me/goals", {
        method: "POST",
        body: {
          title: title.trim(),
          description: description.trim(),
          startDate,
          endDate,
          xpReward,
          goldReward,
        },
      })
      setGoals((currentGoals) => [createdGoal, ...currentGoals])
    } catch (createError) {
      setError(createError.message)
      return
    } finally {
      setSaving(false)
    }
    setTitle("")
    setDescription("")
    setStartDate("")
    setEndDate("")
    setXpReward(200)
    setGoldReward(75)
    setShowCreate(false)
  }
  return (
    <div className="goals-page" style={{ padding: "2rem", maxWidth: "1500px", margin: "0 auto" }}>
      <div className="flex items-center justify-between mb-6">
        <h1
          className="text-2xl font-bold text-slate-1000"
          style={{ fontFamily: "var(--font-display)" }}
        >
          Objetivos
        </h1>
        <button
          type="button" className="challenges-primary-button"
          onClick={() => setShowCreate(true)}
          style={{ background: "var(--color-primary)" , color: "#FFFFFF"}}
        >
          + Nuevo objetivo
        </button>
      </div>

      {error && (
        <div role="alert" className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {error}
        </div>
      )}

      {showCreate && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4"
          style={{ background: "rgba(30,27,75,0.65)" }}
          onClick={() => setShowCreate(false)}
        >
          <div
            className="bg-white rounded-3xl w-full max-w-lg shadow-2xl overflow-hidden animate-fade-up"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="px-6 py-5 border-b border-slate-100 flex items-start justify-between gap-4">
              <div>
                <h2 className="text-xl font-bold text-slate-800">
                  Nuevo objetivo
                </h2>
                <p className="text-sm text-slate-500 mt-1">
                  Define una meta y su recompensa.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowCreate(false)}
                className="w-9 h-9 rounded-xl bg-slate-100 text-slate-500"
              >
                ×
              </button>
            </div>
            <form onSubmit={createGoal} className="p-6 space-y-4">
              <label className="block">
                <span className="text-sm font-semibold text-slate-700">
                  Título
                </span>
                <input
                  required
                  value={title}
                  onChange={(event) => setTitle(event.target.value)}
                  placeholder="Ej. Completar el curso de React"
                  className="mt-1.5 w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none focus:border-indigo-400"
                />
              </label>
              <label className="block">
                <span className="text-sm font-semibold text-slate-700">
                  Descripción
                </span>
                <textarea
                  value={description}
                  onChange={(event) => setDescription(event.target.value)}
                  placeholder="Describe qué quieres conseguir..."
                  rows={3}
                  className="mt-1.5 w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none resize-none focus:border-indigo-400"
                />
              </label>
              <div className="grid grid-cols-2 gap-3">
                <label>
                  <span className="text-sm font-semibold text-slate-700">
                    Fecha de inicio
                  </span>
                  <input
                    required
                    type="date"
                    value={startDate}
                    onChange={(event) => setStartDate(event.target.value)}
                    className="mt-1.5 w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none focus:border-indigo-400"
                  />
                </label>
                <label>
                  <span className="text-sm font-semibold text-slate-700">
                    Fecha límite
                  </span>
                  <input
                    required
                    type="date"
                    min={startDate}
                    value={endDate}
                    onChange={(event) => setEndDate(event.target.value)}
                    className="mt-1.5 w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none focus:border-indigo-400"
                  />
                </label>
              </div>
              <div className="grid grid-cols-2 gap-3">
                <label>
                  <span className="text-sm font-semibold text-slate-700">
                    Recompensa XP
                  </span>
                  <input
                    type="number"
                    min={25}
                    step={25}
                    value={xpReward}
                    onChange={(event) =>
                      setXpReward(Math.max(25, Number(event.target.value)))
                    }
                    className="mt-1.5 w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none focus:border-indigo-400"
                  />
                </label>
                <label>
                  <span className="text-sm font-semibold text-slate-700">
                    Recompensa de oro
                  </span>
                  <input
                    type="number"
                    min={10}
                    step={5}
                    value={goldReward}
                    onChange={(event) =>
                      setGoldReward(Math.max(10, Number(event.target.value)))
                    }
                    className="mt-1.5 w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none focus:border-indigo-400"
                  />
                </label>
              </div>
              <div className="flex gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowCreate(false)}
                  className="flex-1 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold text-slate-600"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 py-2.5 rounded-xl text-white text-sm font-semibold"
                  style={{ background: "var(--color-primary)" }}
                >
                  {saving ? "Guardando…" : "Crear objetivo"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {reward && (
        <div
          className="mb-4 p-4 rounded-2xl flex items-center gap-4 animate-reward"
          style={{
            background: "linear-gradient(135deg, #ede9fe, #ddd6fe)",
            border: "1px solid #c4b5fd",
          }}
        >
          <span className="text-2xl">🎉</span>
          <div>
            <p className="font-bold text-indigo-700">¡Objetivo completado!</p>
            <p className="text-sm text-indigo-500">
              +{reward.xp} XP • +{reward.gold} oro
            </p>
          </div>
        </div>
      )}

      {loading ? (
        <p className="py-8 text-center text-sm text-slate-500">Cargando objetivos…</p>
      ) : goals.length === 0 ? (
        <p className="rounded-2xl border border-slate-100 bg-white py-8 text-center text-sm text-slate-500">
          Aún no tienes objetivos. Crea uno para empezar.
        </p>
      ) : (
        <div className="space-y-4">
          {goals.map((goal) => (
            <div
              key={goal.id}
              className="bg-white rounded-2xl border border-slate-100 overflow-hidden"
            >
              <div className="p-5">
                <div className="flex items-start justify-between gap-3 mb-2">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <h3
                        className="font-bold text-slate-800"
                        style={{ fontFamily: "var(--font-display)" }}
                      >
                        {goal.title}
                      </h3>
                      <StatusBadge status={goal.status} />
                    </div>
                    <p className="text-sm text-slate-500">{goal.description}</p>
                  </div>
                  <div className="text-right text-xs text-slate-400 flex-shrink-0">
                    <p>{goal.startDate}</p>
                    <p>→ {goal.endDate}</p>
                  </div>
                </div>
                <div className="flex items-center gap-4 mt-3 text-xs">
                  <span style={{ color: "#8b5cf6" }}>⭐ {goal.xpReward} XP</span>
                  <span style={{ color: "#d97706" }}>
                    💰 {goal.goldReward} oro
                  </span>
                </div>
              </div>

              {goal.status === "pending" && !evaluating && (
                <div className="px-5 pb-4">
                  <button
                    onClick={() => setEvaluating(goal.id)}
                    className="text-sm font-semibold text-indigo-600 hover:underline"
                  >
                    Finalizar objetivo →
                  </button>
                </div>
              )}

              {evaluating === goal.id && (
                <div className="border-t border-slate-100 px-5 py-4 bg-slate-50">
                  <p className="font-semibold text-slate-700 mb-3 text-sm">
                    ¿Has cumplido este objetivo?
                  </p>
                  <div className="flex gap-3 mb-4">
                    <button
                      onClick={() => evaluate(goal, "completed")}
                      disabled={updatingGoal === goal.id || !rating[goal.id]}
                      className="px-5 py-2.5 rounded-xl text-sm font-semibold text-white transition"
                      style={{
                        background: "#10b981",
                        opacity: updatingGoal === goal.id || !rating[goal.id] ? 0.5 : 1,
                      }}
                    >
                      {updatingGoal === goal.id ? "Guardando…" : "✓ Cumplido"}
                    </button>
                    <button
                      onClick={() => evaluate(goal, "failed")}
                      disabled={updatingGoal === goal.id}
                      className="px-5 py-2.5 rounded-xl text-sm font-semibold transition"
                      style={{ background: "#fee2e2", color: "#ef4444" }}
                    >
                      ✗ No cumplido
                    </button>
                  </div>
                  <p className="text-sm text-slate-600 mb-2">
                    ¿Cómo valorarías tu rendimiento?
                  </p>
                  <div className="flex gap-1.5">
                    {[1, 2, 3, 4, 5].map((n) => (
                      <button
                        key={n}
                        onClick={() => setRating((r) => ({ ...r, [goal.id]: n }))}
                        className="w-8 h-8 rounded-lg text-xs font-bold transition"
                        style={{
                          background:
                            rating[goal.id] === n
                              ? "var(--color-primary)"
                              : "#e2e8f0",
                          color: rating[goal.id] === n ? "white" : "#64748b",
                        }}
                      >
                        {n}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
function StatusBadge({ status }) {
  const map = {
    pending: { label: "Activo", bg: "#e0f2fe", color: "#0284c7" },
    completed: { label: "Completado", bg: "#d1fae5", color: "#059669" },
    failed: { label: "Fallido", bg: "#fee2e2", color: "#ef4444" },
  }
  const { label, bg, color } = map[status] ?? map.pending
  return (
    <span
      className="text-xs px-2 py-0.5 rounded-full font-medium"
      style={{ background: bg, color }}
    >
      {label}
    </span>
  )
}
