import { NavLink } from "react-router-dom"
const NAV = [
  { to: "/", icon: "🏠", label: "Inicio" },
  { to: "/character", icon: "⚔️", label: "Personaje" },
  { to: "/character/inventory", icon: "🎒", label: "Inventario" },
  { to: "/combat", icon: "🗡️", label: "Combate" },
  { to: "/ranking", icon: "🏅", label: "Ranking" },
]
export default function BottomNav() {
  return (
    <nav
      className="fixed bottom-0 left-0 right-0 z-40 lg:hidden flex items-center justify-around px-2 py-2 border-t"
      style={{
        background: "var(--color-sidebar)",
        borderColor: "rgba(99,102,241,0.3)",
      }}
    >
      {NAV.map(({ to, icon, label }) => (
        <NavLink
          key={to}
          to={to}
          end={to === "/" || to === "/character" || to === "/ranking"}
          className="flex flex-col items-center gap-0.5 px-3 py-1.5 rounded-xl transition-all min-w-0"
          style={({ isActive }) => ({
            color: isActive ? "white" : "rgba(199,210,254,0.6)",
            background: isActive ? "rgba(99,102,241,0.35)" : "transparent",
          })}
        >
          <span className="text-xl leading-none">{icon}</span>
          <span className="text-xs font-medium truncate">{label}</span>
        </NavLink>
      ))}
    </nav>
  )
}
