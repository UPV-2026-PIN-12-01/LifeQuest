import { useCallback, useEffect, useState } from 'react'
import './styles/App.css'
import AppRoutes from './routes/AppRoutes';

function App() {
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
    </main>
  )
}

export default App
