import { useEffect } from 'react'
import { sendTaskHeartbeat } from '../services/tasks.js'

const HEARTBEAT_MS = 60_000

// Keeps every open task session alive while this page is visible. Hidden time is not sent, so the
// server does not count it as focus.
export default function useTaskHeartbeats(tasks) {
  const openIds = tasks
    .filter((task) => task.sessionOpen)
    .map((task) => task.id)
    .join(',')

  useEffect(() => {
    if (!openIds) return undefined
    const ids = openIds.split(',')

    function beat() {
      if (document.visibilityState !== 'visible') return
      ids.forEach((id) => sendTaskHeartbeat(id).catch(() => {}))
    }

    beat()
    const timer = setInterval(beat, HEARTBEAT_MS)
    document.addEventListener('visibilitychange', beat)
    return () => {
      clearInterval(timer)
      document.removeEventListener('visibilitychange', beat)
    }
  }, [openIds])
}
