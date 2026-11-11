import AvatarDisplay from "../AvatarDisplay"

const STAT_META = [
  { key: "hp", label: "Vida", icon: "❤️", color: "#ef4444" },
  { key: "strength", label: "Fuerza", icon: "💪", color: "#f59e0b" },
  { key: "knowledge", label: "Conocimiento", icon: "🧠", color: "#6366f1" },
  { key: "speed", label: "Velocidad", icon: "⚡", color: "#0ea5e9" },
  { key: "stamina", label: "Estamina", icon: "🌿", color: "#10b981" },
]

export default function CharacterOverviewView({
  stats,
  displayName,
  playerClass,
  avatar,
}) {
  const xpPct = Math.round((stats.xp / stats.xpToNext) * 100)

  return (
    <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 animate-fade-up">
      <div className="bg-white rounded-3xl border border-slate-100 p-6 flex flex-col items-center">
        <div
          className="w-40 h-40 rounded-3xl overflow-hidden flex items-center justify-center mb-4"
          style={{ background: "linear-gradient(135deg, #1e1b4b, #312e81)" }}
        >
          {avatar?.startsWith("data:") ? (
            <AvatarDisplay avatar={avatar} size={160} className="rounded-3xl" />
          ) : (
            <img
              src={playerClass.image}
              alt={playerClass.name}
              className="w-full h-full object-contain drop-shadow-2xl"
            />
          )}
        </div>
        <h2 className="text-xl font-bold text-slate-800">{displayName}</h2>
        <p className="text-sm text-slate-500 mb-4">
          {playerClass.name} · Nivel {stats.level}
        </p>
        <div className="w-full">
          <div className="flex justify-between text-xs text-slate-500 mb-1.5">
            <span>XP actual</span>
            <span>
              {stats.xp} / {stats.xpToNext}
            </span>
          </div>
          <div className="h-2.5 bg-slate-100 rounded-full overflow-hidden">
            <div
              className="h-full rounded-full xp-bar-fill"
              style={{
                width: `${xpPct}%`,
                background: "linear-gradient(90deg, #6366f1, #8b5cf6)",
              }}
            />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-2 w-full mt-4">
          <div className="rounded-xl p-3 bg-amber-50 text-center">
            <p className="font-bold text-amber-600">💰 {stats.gold}</p>
            <p className="text-xs text-amber-500">Oro</p>
          </div>
          <div className="rounded-xl p-3 bg-indigo-50 text-center">
            <p className="font-bold text-indigo-600">Nv. {stats.level}</p>
            <p className="text-xs text-indigo-400">Nivel</p>
          </div>
        </div>
      </div>
      <div className="lg:col-span-2 grid sm:grid-cols-2 gap-3">
        {STAT_META.map(({ key, label, color , icon}) => {
          const value = Number(stats[key] ?? 0)
          return (
            <div
              key={key}
              className="bg-white rounded-2xl p-4 border border-slate-100 flex items-center gap-4"
            >
              <div
                className="w-11 h-11 rounded-xl flex items-center justify-center text-xl"
                style={{ background: `${color}18` }}
              >
                {icon}
              </div>
              <div className="flex-1">
                <div className="flex justify-between mb-1.5">
                  <p className="text-sm font-semibold text-slate-700">{label}</p>
                  <p className="text-sm font-bold" style={{ color }}>
                    {value}
                  </p>
                </div>
                <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                  <div
                    className="h-full rounded-full"
                    style={{
                      width: `${Math.min(value, 100)}%`,
                      background: color,
                    }}
                  />
                </div>
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}
