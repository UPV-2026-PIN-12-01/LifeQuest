import { useCallback, useEffect, useState } from 'react'
import { Outlet, useNavigate } from 'react-router-dom'
import BottomNav from './BottomNav.jsx'
import Sidebar from './Sidebar.jsx'
import { clearSession } from '../services/session.js'
import { getPlayerProfile } from '../services/player.js'
import { DEFAULT_PLAYER_CLASS } from '../utils/playerClasses.js'

const EMPTY_PLAYER = {
  username: '',
  profileUrl: '',
  playerClass: DEFAULT_PLAYER_CLASS,
  level: 1,
  xp: 0,
  xpForNextLevel: 100,
  gold: 0,
  streakDays: 0,
}

export default function AuthenticatedLayout() {
  const navigate = useNavigate()
  const [player, setPlayer] = useState(EMPTY_PLAYER)
  const [profileError, setProfileError] = useState(null)

  const refreshPlayer = useCallback(async () => {
    try {
      setPlayer(await getPlayerProfile())
      setProfileError(null)
    } catch (error) {
      setProfileError(error)
      if (error.status === 401) navigate('/login', { replace: true })
    }
  }, [navigate])

  useEffect(() => {
    let cancelled = false
    getPlayerProfile().then((profile) => {
      if (cancelled) return
      setPlayer(profile)
      setProfileError(null)
    }).catch((error) => {
      if (cancelled) return
      setProfileError(error)
      if (error.status === 401) navigate('/login', { replace: true })
    })
    return () => {
      cancelled = true
    }
  }, [navigate])

  function handleLogout() {
    clearSession()
    navigate('/login', { replace: true })
  }

  return (
    <>
      <Sidebar player={player} onLogout={handleLogout} />
      <BottomNav />
      <div className="min-h-screen pb-20 lg:ml-64 lg:pb-0">
        {profileError && profileError.status !== 401 && (
          <div
            role="alert"
            className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 pt-4 text-sm text-rose-700 lg:px-6"
          >
            <span>
              {profileError.status === 404
                ? 'No se pudo encontrar el perfil del personaje.'
                : 'No se pudo cargar el perfil del personaje.'}
            </span>
            <button
              type="button"
              onClick={refreshPlayer}
              className="rounded-lg border border-rose-200 px-3 py-1.5 font-semibold"
            >
              Reintentar
            </button>
          </div>
        )}
        <Outlet context={{ player, refreshPlayer }} />
      </div>
    </>
  )
}
