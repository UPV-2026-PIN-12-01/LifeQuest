import { useEffect, useState } from 'react'
import { useNavigate, useOutletContext } from 'react-router-dom'
import DailyQuests from '../components/home/DailyQuests.jsx'
import PlayerHero from '../components/home/PlayerHero.jsx'
import TaskFormModal from '../components/home/TaskFormModal.jsx'
import TaskTable from '../components/home/TaskTable.jsx'
import WeekGrid from '../components/home/WeekGrid.jsx'
import WeekHeader from '../components/home/WeekHeader.jsx'
import useTaskHeartbeats from '../hooks/useTaskHeartbeats.js'
import { getDailyQuests } from '../services/player.js'
import { getCachedTasksForWeek, getTasksForWeek, setCachedTasksForWeek } from '../services/tasks.js'
import { addDays, startOfWeek, toIsoDate, weekRangeLabel } from '../utils/week.js'
import '../styles/home.css'

export default function HomePage() {
  const navigate = useNavigate()
  const { player, refreshPlayer } = useOutletContext()
  const [weekOffset, setWeekOffset] = useState(0)
  const [taskData, setTaskData] = useState(() => {
    const initialWeek = startOfWeek(new Date())
    return {
      weekKey: initialWeek.getTime(),
      tasks: getCachedTasksForWeek(initialWeek) ?? [],
    }
  })
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
  const cachedTasks = getCachedTasksForWeek(weekStart)
  const tasks = taskData.weekKey === weekKey ? taskData.tasks : cachedTasks ?? []
  useTaskHeartbeats(tasks)

  const completedTasks = tasks.filter((task) => task.status === 'completed')
  const weekStats = {
    done: completedTasks.length,
    total: tasks.length,
    xp: completedTasks.reduce((sum, task) => sum + task.xp, 0),
  }

  useEffect(() => {
    let cancelled = false
    getDailyQuests().then((dailyQuests) => {
      if (cancelled) return
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
    const requestedWeekStart = new Date(weekKey)
    getTasksForWeek(requestedWeekStart).then((weekTasks) => {
      if (!cancelled) {
        setTaskData({ weekKey, tasks: weekTasks })
        setCachedTasksForWeek(requestedWeekStart, weekTasks)
      }
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
        <section id="week" className="week-panel">
          <WeekHeader
            label={weekOffset === 0 ? 'Semana actual' : 'Semana'}
            rangeLabel={weekRangeLabel(weekStart)}
            onPrevious={() => setWeekOffset((offset) => offset - 1)}
            onNext={() => setWeekOffset((offset) => offset + 1)}
            onAddTask={() => setEditor({ task: null })}
          />
          <WeekGrid weekStart={weekStart} tasks={tasks} onSelectTask={(task) => setEditor({ task })} />
        </section>
        <TaskTable
          id="tasks"
          tasks={tasks}
          onTaskChanged={(changed) => {
            setTaskData((current) => {
              const currentTasks = current.weekKey === weekKey ? current.tasks : cachedTasks ?? []
              const next = currentTasks.map((task) => (task.id === changed.id ? changed : task))
              setCachedTasksForWeek(weekStart, next)
              return { weekKey, tasks: next }
            })
            refreshPlayer()
          }}
        />
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
