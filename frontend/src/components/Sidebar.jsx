import { Link, useLocation } from 'react-router-dom'
import { classById } from '../utils/playerClasses.js'

const NAV = [
  { to: '/home', icon: '🏠', label: 'Inicio' },
  { to: '/character', icon: '⚔️', label: 'Personaje' },
  { to: '/settings', icon: '⚙️', label: 'Ajustes' },
]

function isSelected(to, pathname, hash) {
  const [targetPath, targetHash] = to.split('#')
  if (targetPath !== pathname) return false
  return targetHash ? `#${targetHash}` === hash : !hash
}

function AccountAvatar({ profileUrl, username }) {
  if (profileUrl) {
    return (
      <img
        src={profileUrl}
        alt=""
        className="h-10 w-10 flex-shrink-0 rounded-full object-cover"
      />
    )
  }

  return (
    <div
      aria-hidden="true"
      className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full font-bold text-white"
      style={{ background: 'var(--lq-violet-500)' }}
    >
      {(username || 'A')[0].toUpperCase()}
    </div>
  )
}

export default function Sidebar({ player, onLogout }) {
  const { pathname, hash } = useLocation()
  const playerClass = classById(player.playerClass)
  const xp = Number(player.xp) || 0
  const xpGoal = Number(player.xpForNextLevel) || 1
  const xpPercent = Math.min(100, Math.round((xp / xpGoal) * 100))

  return (
    <aside
      className="fixed left-0 top-0 z-40 hidden h-screen w-64 flex-col lg:flex"
      style={{ background: 'var(--lq-violet-900)' }}
    >
      <div className="border-b px-6 pb-5 pt-7" style={{ borderColor: 'rgba(196,181,253,0.18)' }}>
        <Link to="/home" className="flex items-center gap-3 no-underline">
          <span
            aria-hidden="true"
            className="flex h-9 w-9 items-center justify-center rounded-xl text-lg"
            style={{ background: 'var(--lq-violet-500)' }}
          >
            ⚔️
          </span>
          <span className="text-xl font-bold text-white" style={{ fontFamily: 'var(--font-display)' }}>
            LifeQuest
          </span>
        </Link>
      </div>

      <div
        className="mx-3 mt-4 rounded-xl px-4 py-4"
        style={{ background: 'rgba(109,94,252,0.22)' }}
      >
        <div className="mb-3 flex items-center gap-3">
          <AccountAvatar profileUrl={player.profileUrl} username={player.username} />
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold leading-tight text-white">
              {player.username || 'Aventurero'}
            </p>
            <p className="truncate text-xs" style={{ color: 'var(--lq-violet-200)' }}>
              {playerClass.name}
            </p>
            <p className="text-xs" style={{ color: 'var(--lq-violet-200)' }}>
              Nivel {player.level}
            </p>
          </div>
        </div>

        <div
          className="mb-1.5 flex justify-between text-xs"
          style={{ color: 'var(--lq-violet-200)' }}
        >
          <span>XP</span>
          <span>{xp} / {xpGoal}</span>
        </div>
        <div className="h-1.5 overflow-hidden rounded-full bg-white/10">
          <div
            className="h-full rounded-full transition-all"
            style={{ width: `${xpPercent}%`, background: 'var(--lq-gold)' }}
          />
        </div>
        <div className="mt-3 flex gap-4 text-xs" style={{ color: 'var(--lq-violet-200)' }}>
          <span>💰 {player.gold ?? 0}</span>
          <span>🔥 {player.streakDays ?? 0} días</span>
        </div>
      </div>

      <nav aria-label="Navegación principal" className="mt-4 flex-1 space-y-1 overflow-y-auto px-3">
        {NAV.map(({ to, icon, label }) => {
          const selected = isSelected(to, pathname, hash)
          return (
            <Link
              key={to}
              to={to}
              aria-current={selected ? 'page' : undefined}
              className="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors"
              style={{
                background: selected ? 'var(--lq-violet-500)' : 'transparent',
                color: selected ? 'white' : 'var(--lq-violet-200)',
              }}
            >
              <span aria-hidden="true" className="text-base">{icon}</span>
              {label}
            </Link>
          )
        })}
      </nav>

      <div className="space-y-1 border-t px-3 pb-5 pt-3" style={{ borderColor: 'rgba(196,181,253,0.18)' }}>
        <button
          type="button"
          onClick={onLogout}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors hover:text-white"
          style={{ color: 'var(--lq-violet-200)', background: 'transparent' }}
        >
          <span aria-hidden="true" className="text-base">🚪</span>
          Cerrar sesión
        </button>
        <p className="px-3 text-xs" style={{ color: 'rgba(196,181,253,0.55)' }}>
          LifeQuest · MVP
        </p>
      </div>
    </aside>
  )
}
