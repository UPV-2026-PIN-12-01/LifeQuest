import { Link, useLocation } from 'react-router-dom'
const NAV = [
  { to: '/home', icon: '🏠', label: 'Inicio' },
  { to: '/home#week', icon: '📅', label: 'Semana' },
  { to: '/home#tasks', icon: '✅', label: 'Tareas' },
  { to: '/character', icon: '⚔️', label: 'Personaje' },
  { to: '/settings', icon: '⚙️', label: 'Ajustes' },
]
export default function BottomNav() {
  const { pathname, hash } = useLocation()

  return (
    <nav
      aria-label="Navegación principal"
      className="fixed bottom-0 left-0 right-0 z-40 lg:hidden flex items-center justify-around px-2 py-2 border-t"
      style={{
        background: "var(--color-sidebar)",
        borderColor: "rgba(99,102,241,0.3)",
      }}
    >
      {NAV.map(({ to, icon, label }) => {
        const [targetPath, targetHash] = to.split('#')
        const active = targetPath === pathname
          && (targetHash ? `#${targetHash}` === hash : !hash)
        return (
          <Link
            key={to}
            to={to}
            aria-current={active ? 'page' : undefined}
            className="flex min-w-0 flex-col items-center gap-0.5 rounded-xl px-2 py-1.5 transition-all"
            style={{
              color: active ? 'white' : 'rgba(199,210,254,0.6)',
              background: active ? 'rgba(99,102,241,0.35)' : 'transparent',
            }}
          >
            <span aria-hidden="true" className="text-xl leading-none">{icon}</span>
            <span className="truncate text-xs font-medium">{label}</span>
          </Link>
        )
      })}
    </nav>
  )
}
