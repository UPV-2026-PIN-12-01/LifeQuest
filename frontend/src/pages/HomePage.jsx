import { useEffect, useState } from 'react'
import DailyQuests from '../components/home/DailyQuests.jsx'
import PlayerHero from '../components/home/PlayerHero.jsx'
import WeekGrid from '../components/home/WeekGrid.jsx'
import WeekHeader from '../components/home/WeekHeader.jsx'
import { getDailyQuests, getPlayerProfile } from '../services/player.js'
import { getTasksForWeek } from '../services/tasks.js'
import { DEFAULT_PLAYER_CLASS } from '../utils/playerClasses.js'
import { addDays, startOfWeek, weekRangeLabel } from '../utils/week.js'
import '../styles/home.css'

const EMPTY_PLAYER = {
  username: '',
  playerClass: DEFAULT_PLAYER_CLASS,
  level: 1,
  xp: 0,
  xpForNextLevel: 100,
  streakDays: 0,
}

export default function HomePage() {
  const [weekOffset, setWeekOffset] = useState(0)
  const [tasks, setTasks] = useState([])
  const [player, setPlayer] = useState(EMPTY_PLAYER)
  const [quests, setQuests] = useState([])

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
    })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    getTasksForWeek(new Date(weekKey)).then((weekTasks) => {
      if (!cancelled) setTasks(weekTasks)
    })
    return () => {
      cancelled = true
    }
  }, [weekKey])

  return (
    <main className="home-page">
      <PlayerHero player={player} weekStats={weekStats} />
      <section className="week-panel">
        <WeekHeader
          label={weekOffset === 0 ? 'Semana actual' : 'Semana'}
          rangeLabel={weekRangeLabel(weekStart)}
          onPrevious={() => setWeekOffset((offset) => offset - 1)}
          onNext={() => setWeekOffset((offset) => offset + 1)}
        />
        <WeekGrid weekStart={weekStart} tasks={tasks} />
      </section>
      {quests.length > 0 && <DailyQuests quests={quests} />}
    </main>
  )
}
