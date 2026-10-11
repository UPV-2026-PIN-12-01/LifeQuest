import { useEffect, useState } from 'react'
import PlayerHero from '../components/home/PlayerHero.jsx'
import StatusLegend from '../components/home/StatusLegend.jsx'
import WeekGrid from '../components/home/WeekGrid.jsx'
import WeekHeader from '../components/home/WeekHeader.jsx'
import { getPlayerProfile } from '../services/player.js'
import { getTasksForWeek, getWeekSummary } from '../services/tasks.js'
import { DEFAULT_PLAYER_CLASS } from '../utils/playerClasses.js'
import { addDays, startOfWeek, weekRangeLabel } from '../utils/week.js'
import '../styles/home.css'

const EMPTY_SUMMARY = { activeChallenges: 0, trainingInProgress: 0, weeklyXp: 0 }
const EMPTY_PLAYER = { username: '', playerClass: DEFAULT_PLAYER_CLASS, level: 1, xp: 0, xpForNextLevel: 100 }

export default function HomePage() {
  const [weekOffset, setWeekOffset] = useState(0)
  const [tasks, setTasks] = useState([])
  const [summary, setSummary] = useState(EMPTY_SUMMARY)
  const [player, setPlayer] = useState(EMPTY_PLAYER)

  const weekStart = addDays(startOfWeek(new Date()), weekOffset * 7)
  const weekKey = weekStart.getTime()

  useEffect(() => {
    let cancelled = false
    getPlayerProfile().then((profile) => {
      if (!cancelled) setPlayer(profile)
    })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    const start = new Date(weekKey)
    Promise.all([getTasksForWeek(start), getWeekSummary(start)]).then(([weekTasks, weekSummary]) => {
      if (cancelled) return
      setTasks(weekTasks)
      setSummary(weekSummary)
    })
    return () => {
      cancelled = true
    }
  }, [weekKey])

  return (
    <main className="home-page">
      <PlayerHero player={player} summary={summary} />
      <section className="week-panel">
        <WeekHeader
          label={weekOffset === 0 ? 'Semana actual' : weekRangeLabel(weekStart)}
          onPrevious={() => setWeekOffset((offset) => offset - 1)}
          onNext={() => setWeekOffset((offset) => offset + 1)}
        />
        <WeekGrid weekStart={weekStart} tasks={tasks} />
        <StatusLegend />
      </section>
    </main>
  )
}
