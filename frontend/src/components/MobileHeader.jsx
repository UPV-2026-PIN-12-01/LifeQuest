import { useState } from "react"
import { NavLink } from "react-router-dom"
import { CLASS_META } from "../pages/Login"
const ALL_NAV = [
  { to: "/", icon: "🏠", label: "Inicio" },
  { to: "/character", icon: "⚔️", label: "Personaje" },
  { to: "/character/inventory", icon: "🎒", label: "Inventario" },
  { to: "/character/shop", icon: "🏪", label: "Tienda" },
  { to: "/combat", icon: "🗡️", label: "Combate" },
  { to: "/ranking", icon: "🏅", label: "Clasificación" },
  { to: "/ranking/friends", icon: "👥", label: "Amigos" },
  { to: "/dashboard", icon: "📊", label: "Dashboard" },
  { to: "/challenges", icon: "🎯", label: "Desafíos" },
  { to: "/achievements", icon: "🏆", label: "Logros" },
  { to: "/ai", icon: "🤖", label: "LifeQuest IA" },
  { to: "/settings", icon: "⚙️", label: "Ajustes" },
]
function AccountAvatar({ profilePhoto, username, size = 32 }) {
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
        fontSize: size * 0.42,
      }}
    >
      {initial}
    </div>
  )
}
export default function MobileHeader({
  stats,
  playerName = "Aventurero",
  avatar: _avatar,
  username = "aventurero",
  profilePhoto = "",
  characterClass = "warrior",
  onLogout,
}) {
  const [open, setOpen] = useState(false)
  const xpPct = Math.round((stats.xp / stats.xpToNext) * 100)
  const classMeta = CLASS_META[characterClass]
  return (
    <>
      <header
        className="lg:hidden fixed top-0 left-0 right-0 z-40 flex items-center justify-between px-4 h-14 border-b"
        style={{
          background: "var(--color-sidebar)",
          borderColor: "rgba(99,102,241,0.3)",
        }}
      >
        <div className="flex items-center gap-2">
          <span className="text-lg">⚔️</span>
          <span
            className="text-white font-bold text-lg"
            style={{ fontFamily: "var(--font-display)" }}
          >
            LifeQuest
          </span>
        </div>
        <div className="flex items-center gap-3">
          <div
            className="flex items-center gap-1.5 text-xs"
            style={{ color: "#c7d2fe" }}
          >
            <span>⭐ Nv.{stats.level}</span>
            <span>💰{stats.gold}</span>
          </div>
          <button
            onClick={() => setOpen((o) => !o)}
            className="w-8 h-8 flex flex-col justify-center gap-1.5 items-center rounded-lg"
            style={{ color: "#c7d2fe" }}
          >
            <span
              className="w-5 h-0.5 bg-current rounded transition-all"
              style={{
                transform: open ? "rotate(45deg) translateY(4px)" : "none",
              }}
            />
            <span
              className="w-5 h-0.5 bg-current rounded transition-all"
              style={{ opacity: open ? 0 : 1 }}
            />
            <span
              className="w-5 h-0.5 bg-current rounded transition-all"
              style={{
                transform: open ? "rotate(-45deg) translateY(-4px)" : "none",
              }}
            />
          </button>
        </div>
      </header>

      {open && (
        <div
          className="lg:hidden fixed inset-0 z-50 flex"
          onClick={() => setOpen(false)}
        >
          <div
            className="absolute inset-0"
            style={{ background: "rgba(15,10,40,0.6)" }}
          />
          <div
            className="relative w-72 h-full flex flex-col shadow-2xl"
            style={{ background: "var(--color-sidebar)" }}
            onClick={(e) => e.stopPropagation()}
          >
            {/* Player card */}
            <div
              className="px-4 pt-6 pb-4 border-b"
              style={{ borderColor: "rgba(99,102,241,0.3)" }}
            >
              <div className="flex items-center gap-3 mb-3">
                <AccountAvatar
                  profilePhoto={profilePhoto}
                  username={username}
                  size={44}
                />
                <div className="min-w-0">
                  <p
                    className="text-white text-sm font-semibold truncate"
                    style={{ fontFamily: "var(--font-display)" }}
                  >
                    {username}
                  </p>
                  <p className="text-xs truncate" style={{ color: "#c7d2fe" }}>
                    {playerName} · {classMeta.label}
                  </p>
                  <p className="text-xs" style={{ color: "#c7d2fe" }}>
                    Nivel {stats.level}
                  </p>
                </div>
              </div>
              <div
                className="h-1.5 rounded-full overflow-hidden mb-1"
                style={{ background: "rgba(255,255,255,0.1)" }}
              >
                <div
                  className="h-full rounded-full"
                  style={{ width: `${xpPct}%`, background: "#8b5cf6" }}
                />
              </div>
              <div className="flex gap-4 text-xs" style={{ color: "#c7d2fe" }}>
                <span>💰 {stats.gold}</span>
                <span>🔥 {stats.streak} días</span>
              </div>
            </div>
            <nav className="flex-1 px-3 py-3 space-y-1 overflow-y-auto">
              {ALL_NAV.map(({ to, icon, label }) => (
                <NavLink
                  key={to}
                  to={to}
                  end={to === "/" || to === "/character" || to === "/ranking"}
                  onClick={() => setOpen(false)}
                  className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all"
                  style={({ isActive }) => ({
                    background: isActive
                      ? "var(--color-primary)"
                      : "transparent",
                    color: isActive ? "white" : "#c7d2fe",
                  })}
                >
                  <span>{icon}</span>
                  {label}
                </NavLink>
              ))}
            </nav>
            <div className="px-3 pb-2">
              <NavLink
                to="/training"
                onClick={() => setOpen(false)}
                className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-bold transition-all"
                style={({ isActive }) => ({
                  background: isActive
                    ? "linear-gradient(135deg,#ef4444,#f97316)"
                    : "rgba(239,68,68,0.15)",
                  color: isActive ? "white" : "#fca5a5",
                  border: "1px solid rgba(239,68,68,0.3)",
                })}
              >
                <span>🔥</span> Modo Entrenamiento
              </NavLink>
            </div>
            <div
              className="px-3 pb-6 border-t pt-3"
              style={{ borderColor: "rgba(99,102,241,0.3)" }}
            >
              <button
                onClick={onLogout}
                className="sidebar-btn w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium"
                style={{ color: "#c7d2fe" }}
              >
                <span>🚪</span> Cerrar sesión
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
