import { NavLink } from "react-router-dom"
import { CLASS_META } from "../pages/Login"
const NAV = [
  { to: "/", icon: "🏠", label: "Inicio" },
  { to: "/character", icon: "⚔️", label: "Personaje" },
  { to: "/character/inventory", icon: "🎒", label: "Inventario", nested: true },
  { to: "/character/shop", icon: "🏪", label: "Tienda", nested: true },
  { to: "/combat", icon: "🗡️", label: "Combate" },
  { to: "/ranking", icon: "🏅", label: "Clasificación" },
  { to: "/ranking/friends", icon: "👥", label: "Amigos", nested: true },
  { to: "/dashboard", icon: "📊", label: "Dashboard" },
  { to: "/goals", icon: "🚩", label: "Objetivos" },
  { to: "/challenges", icon: "🎯", label: "Desafíos" },
  { to: "/achievements", icon: "🏆", label: "Logros" },
  { to: "/ai", icon: "🤖", label: "LifeQuest IA" },
  { to: "/settings", icon: "⚙️", label: "Ajustes" },
]
const NAV_TRAINING = {
  to: "/training",
  icon: "🔥",
  label: "Modo Entrenamiento",
}
function AccountAvatar({ profilePhoto, username, size = 40 }) {
  if (profilePhoto) {
    return (
      <div
        className="rounded-full overflow-hidden flex-shrink-0"
        style={{ width: size, height: size }}
      >
        <img
          src={profilePhoto}
          alt="perfil"
          className="w-full h-full object-cover"
        />
      </div>
    )
  }
  const initial = (username || "A")[0].toUpperCase()
  return (
    <div
      className="rounded-full flex items-center justify-center flex-shrink-0 font-bold"
      style={{
        width: size,
        height: size,
        background: "var(--color-primary)",
        color: "white",
        fontSize: size * 0.4,
      }}
    >
      {initial}
    </div>
  )
}
export default function Sidebar({
  stats,
  playerName = "Aventurero",
  avatar: _avatar,
  username = "aventurero",
  profilePhoto = "",
  characterClass = "warrior",
  onLogout,
}) {
  const xpPct = Math.round((stats.xp / stats.xpToNext) * 100)
  const classMeta = CLASS_META[characterClass]
  return (
    <aside
      className="hidden lg:flex fixed left-0 top-0 h-screen w-64 flex-col z-40"
      style={{ background: "var(--color-sidebar)" }}
    >
      {/* Logo */}
      <div className="px-6 pt-7 pb-5 border-b border-indigo-900">
        <div className="flex items-center gap-3">
          <div
            className="w-9 h-9 rounded-xl flex items-center justify-center text-lg"
            style={{ background: "var(--color-primary)" }}
          >
            ⚔️
          </div>
          <span
            className="text-white text-xl font-bold"
            style={{ fontFamily: "var(--font-display)" }}
          >
            LifeQuest
          </span>
        </div>
      </div>

      {/* Player card */}
      <div
        className="px-4 py-4 mx-3 mt-4 rounded-xl"
        style={{ background: "rgba(99,102,241,0.2)" }}
      >
        <div className="flex items-center gap-3 mb-3">
          <AccountAvatar
            profilePhoto={profilePhoto}
            username={username}
            size={40}
          />
          <div className="min-w-0">
            <p
              className="text-white text-sm font-semibold leading-tight truncate"
              style={{ fontFamily: "var(--font-display)" }}
            >
              {username}
            </p>
            <p
              className="text-xs truncate"
              style={{ color: "var(--color-sidebar-text)" }}
            >
              {playerName} · {classMeta.label}
            </p>
            <p
              className="text-xs"
              style={{ color: "var(--color-sidebar-text)" }}
            >
              Nivel {stats.level}
            </p>
          </div>
        </div>
        <div
          className="flex items-center justify-between text-xs mb-1.5"
          style={{ color: "var(--color-sidebar-text)" }}
        >
          <span>XP</span>
          <span>
            {stats.xp} / {stats.xpToNext}
          </span>
        </div>
        <div
          className="h-1.5 rounded-full overflow-hidden"
          style={{ background: "rgba(255,255,255,0.1)" }}
        >
          <div
            className="h-full rounded-full transition-all"
            style={{ width: `${xpPct}%`, background: "var(--color-accent-xp)" }}
          />
        </div>
        <div
          className="flex gap-4 mt-3 text-xs"
          style={{ color: "var(--color-sidebar-text)" }}
        >
          <span>💰 {stats.gold}</span>
          <span>🔥 {stats.streak} días</span>
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-3 mt-4 space-y-1 overflow-y-auto">
        {NAV.map(({ to, icon, label, nested }) => (
          <NavLink
            key={to}
            to={to}
            end={to === "/" || to === "/character" || to === "/ranking"}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all ${
                nested ? "ml-4" : ""
              } ${isActive ? "text-white" : "hover:text-white"}`
            }
            style={({ isActive }) => ({
              background: isActive ? "var(--color-primary)" : "transparent",
              color: isActive ? "white" : "var(--color-sidebar-text)",
            })}
          >
            <span className="text-base">{icon}</span>
            {label}
          </NavLink>
        ))}
      </nav>

      {/* Training mode */}
      <div className="px-3 pb-2">
        <NavLink
          to={NAV_TRAINING.to}
          className={({ isActive }) =>
            `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-bold transition-all ${
              isActive ? "text-white" : ""
            }`
          }
          style={({ isActive }) => ({
            background: isActive
              ? "linear-gradient(135deg,#ef4444,#f97316)"
              : "rgba(239,68,68,0.15)",
            color: isActive ? "white" : "#fca5a5",
            border: "1px solid rgba(239,68,68,0.3)",
          })}
        >
          <span className="text-base">{NAV_TRAINING.icon}</span>
          {NAV_TRAINING.label}
        </NavLink>
      </div>

      <div className="px-3 pb-5 border-t border-indigo-900 pt-3 space-y-1">
        <button
          onClick={onLogout}
          className="sidebar-btn w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all"
          style={{
            color: "var(--color-sidebar-text)",
            background: "transparent",
          }}
        >
          <span className="text-base">🚪</span>
          Cerrar sesión
        </button>
        <p className="px-3 text-xs" style={{ color: "rgba(199,210,254,0.35)" }}>
          LifeQuest v0.1 • MVP
        </p>
      </div>
    </aside>
  )
}
