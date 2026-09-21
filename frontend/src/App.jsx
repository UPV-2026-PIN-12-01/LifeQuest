import { useCallback, useEffect, useState } from 'react'
import { Analytics } from '@vercel/analytics/react'
import './App.css'

function App() {
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const loadHello = useCallback(() => {
    setLoading(true)
    setError('')
    fetch('/api/hello')
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`)
        }
        return response.json()
      })
      .then((data) => {
        setMessage(data.message)
      })
      .catch((err) => {
        setMessage('')
        setError(err.message)
      })
      .finally(() => {
        setLoading(false)
      })
  }, [])

  useEffect(() => {
    loadHello()
  }, [loadHello])

  return (
    <main className="page">
      <h1>LifeQuest</h1>
      <p className="subtitle">PIN 2026 · Grupo 12.01</p>
      {loading && <p>Loading backend…</p>}
      {!loading && message && <p className="message">{message}</p>}
      {!loading && error && (
        <p className="error">
          Could not reach the API ({error}). Start the backend on port 8080.
        </p>
      )}
      <button type="button" onClick={loadHello} disabled={loading}>
        Call /api/hello
      </button>
      <Analytics />
    </main>
  )
}

export default App
