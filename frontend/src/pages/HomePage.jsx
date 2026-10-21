import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import DailyQuests from '../components/home/DailyQuests.jsx'
import PlayerHero from '../components/home/PlayerHero.jsx'
import TaskTable from '../components/home/TaskTable.jsx'
import WeekGrid from '../components/home/WeekGrid.jsx'
import WeekHeader from '../components/home/WeekHeader.jsx'
import { getDailyQuests, getPlayerProfile } from '../services/player.js'
import { getTasksForWeek } from '../services/tasks.js'
import { DEFAULT_PLAYER_CLASS } from '../utils/playerClasses.js'
import { addDays, startOfWeek, toIsoDate, weekRangeLabel } from '../utils/week.js'
import '../styles/home.css'

const EMPTY_PLAYER = {
  username: '',
  profileUrl: '',
  playerClass: DEFAULT_PLAYER_CLASS,
  level: 1,
  xp: 0,
  xpForNextLevel: 100,
  streakDays: 0,
}

export default function HomePage() {
  const navigate = useNavigate()
  const [weekOffset, setWeekOffset] = useState(0)
  const [tasks, setTasks] = useState([])
  const [player, setPlayer] = useState(EMPTY_PLAYER)
  const [quests, setQuests] = useState([])
  const [loadError, setLoadError] = useState(null)
  // null when closed, { task: null } to create, { task } to edit.
  const [editor, setEditor] = useState(null)
  const [tasksVersion, setTasksVersion] = useState(0)

  // An expired or missing session sends the user back to log in.
  useEffect(() => {
    if (loadError?.status === 401) navigate('/login', { replace: true })
  }, [loadError, navigate])

  const weekStart = addDays(startOfWeek(new Date()), weekOffset * 7)
  const weekKey = weekStart.getTime()

  const completedTasks = tasks.filter((task) => task.status === 'completed')
  const weekStats = {
    done: completedTasks.length,
    total: tasks.length,
    xp: completedTasks.reduce((sum, task) => sum + task.xp, 0),
  }

  useEffect(() => {
    let cancelled = false
    Promise.all([getPlayerProfile(), getDailyQuests()]).then(([profile, dailyQuests]) => {
      if (cancelled) return
      setPlayer(profile)
      setQuests(dailyQuests)
    }).catch((error) => {
      if (!cancelled) setLoadError(error)
    })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    getTasksForWeek(new Date(weekKey)).then((weekTasks) => {
      if (!cancelled) setTasks(weekTasks)
    }).catch((error) => {
      if (!cancelled) setLoadError(error)
    })
    return () => {
      cancelled = true
    }
  }, [weekKey, tasksVersion])

  return (
    <main className="home-page">
      {loadError && (
        <p className="home-error" role="alert">
          {loadErrorMessage(loadError)}
        </p>
      )}
      <PlayerHero player={player} weekStats={weekStats} />
      <div className="calendar-task-layout">
        <section className="week-panel">
          <WeekHeader
            label={weekOffset === 0 ? 'Semana actual' : 'Semana'}
            rangeLabel={weekRangeLabel(weekStart)}
            onPrevious={() => setWeekOffset((offset) => offset - 1)}
            onNext={() => setWeekOffset((offset) => offset + 1)}
          />
          <WeekGrid weekStart={weekStart} tasks={tasks} />
        </section>
        <TaskTable tasks={tasks} />
      </div>
      {quests.length > 0 && <DailyQuests quests={quests} />}
      {editor && (
        <TaskFormModal
          task={editor.task}
          defaultDate={toIsoDate(new Date())}
          onClose={() => setEditor(null)}
          onSaved={() => {
            setEditor(null)
            setTasksVersion((version) => version + 1)
          }}
        />
      )}
    </main>
  )
}

function loadErrorMessage(error) {
  if (error.status === 401) return 'Tu sesión ha caducado. Vuelve a iniciar sesión.'
  if (error.status === 404) return 'Tu usuario no tiene personaje todavía.'
  return 'No se pudieron cargar tus datos. Inténtalo de nuevo más tarde.'
}
