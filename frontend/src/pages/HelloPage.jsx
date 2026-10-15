 
import { useState, useEffect } from 'react';

export default function HelloPage() {
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState(null);

  const loadHello = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch('http://localhost:8080/api/hello');
      const data = await res.text();
      setMessage(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // El return DEBE ir aquí dentro de la función del componente
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
  );
}