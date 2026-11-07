import { useEffect, useRef, useState } from 'react'
import { useNavigate, useOutletContext } from 'react-router-dom'
import { loadChatSession, saveChatSession, sendChatMessage } from '../services/chat.js'
import { getTasksForWeek } from '../services/tasks.js'
import { startOfWeek, toIsoDate, weekRangeLabel } from '../utils/week.js'
import '../styles/guide.css'

const PROMPTS = ['Revisa mi semana', 'Qué hago hoy', 'Propón un desafío']
const MAX_MESSAGE_LENGTH = 2000
const GREETING = 'Dime qué quieres revisar. Puedo mirar tu semana, tus misiones de hoy y tus desafíos activos.'

function newThread(id) {
  return {
    id,
    title: 'Chat nuevo',
    day: 'Hoy',
    preview: 'Sin mensajes todavía',
    messages: [{ role: 'assistant', text: GREETING }],
  }
}

function chatErrorText(error) {
  if (error?.status === 0) return 'No hay conexión. Inténtalo de nuevo.'
  if (error?.message === 'message must be at most 2000 characters') {
    return 'El mensaje puede tener como máximo 2000 caracteres.'
  }
  if (error?.status === 502 || error?.message === 'Chat is unavailable') {
    return 'El consejero no está disponible ahora. Inténtalo de nuevo.'
  }
  return 'No se pudo enviar el mensaje.'
}

export default function GuidePage() {
  const { player } = useOutletContext()
  const navigate = useNavigate()
  const saved = loadChatSession()
  const [threads, setThreads] = useState(() => saved?.threads ?? [newThread('chat-1')])
  const [activeId, setActiveId] = useState(() => saved?.activeId ?? 'chat-1')
  const [draft, setDraft] = useState('')
  const [historyOpen, setHistoryOpen] = useState(false)
  const [taskCount, setTaskCount] = useState(null)
  const [threadSeq, setThreadSeq] = useState(() => saved?.threadSeq ?? 1)
  const [pendingId, setPendingId] = useState(null)
  const messagesRef = useRef(null)
  const weekStart = startOfWeek(new Date())
  const weekKey = toIsoDate(weekStart)
  const thread = threads.find((item) => item.id === activeId) ?? threads[0]
  const pending = pendingId !== null

  useEffect(() => {
    saveChatSession({ threads, activeId, threadSeq })
  }, [threads, activeId, threadSeq])

  useEffect(() => {
    let cancelled = false
    getTasksForWeek(startOfWeek(new Date())).then((tasks) => {
      if (!cancelled) setTaskCount(tasks.length)
    }).catch(() => {})
    return () => {
      cancelled = true
    }
  }, [weekKey])

  useEffect(() => {
    const node = messagesRef.current
    if (node) node.scrollTop = node.scrollHeight
  }, [thread, pendingId])

  const reading = [
    `Semana ${weekRangeLabel(weekStart)}`,
    taskCount == null ? null : `${taskCount} tareas`,
    `racha ${player.streakDays ?? 0}`,
  ].filter(Boolean).join(' · ')

  function selectThread(id) {
    setActiveId(id)
    setHistoryOpen(false)
  }

  function startChat() {
    if (pending) return
    const id = `chat-${threadSeq + 1}`
    setThreadSeq((value) => value + 1)
    setThreads((current) => [newThread(id), ...current])
    setActiveId(id)
    setDraft('')
    setHistoryOpen(false)
  }

  function appendMessage(threadId, message) {
    setThreads((current) => current.map((item) => {
      if (item.id !== threadId) return item
      return { ...item, messages: [...item.messages, message] }
    }))
  }

  async function sendMessage(event) {
    event.preventDefault()
    const text = draft.trim()
    if (!text || pending) return
    const threadId = activeId
    setDraft('')
    setThreads((current) => current.map((item) => {
      if (item.id !== threadId) return item
      return {
        ...item,
        title: item.title === 'Chat nuevo' ? text.slice(0, 32) : item.title,
        preview: text,
        messages: [...item.messages, { role: 'user', text }],
      }
    }))
    setPendingId(threadId)
    try {
      const payload = await sendChatMessage(text)
      const reply = typeof payload?.reply === 'string' ? payload.reply.trim() : ''
      if (!reply) throw Object.assign(new Error('Chat is unavailable'), { status: 502 })
      setThreads((current) => current.map((item) => {
        if (item.id !== threadId) return item
        return {
          ...item,
          preview: reply.replace(/\s+/g, ' ').slice(0, 80),
          messages: [...item.messages, { role: 'assistant', text: reply }],
        }
      }))
    } catch (error) {
      if (error?.status === 401) {
        navigate('/login', { replace: true })
        return
      }
      appendMessage(threadId, { role: 'error', text: chatErrorText(error) })
      setDraft((current) => current || text)
    } finally {
      setPendingId(null)
    }
  }

  return (
    <main className="guide-page">
      <section className={`guide-panel guide-history${historyOpen ? ' open' : ''}`} aria-label="Historial de chats">
        <div className="guide-panel-head"><h2>Chats</h2></div>
        <button className="guide-new-chat" type="button" onClick={startChat} disabled={pending}>+ Nuevo chat</button>
        <div className="guide-history-list">
          <h3>Hoy</h3>
          {threads.map((item) => (
            <button
              key={item.id}
              type="button"
              className={`guide-thread${item.id === thread.id ? ' active' : ''}`}
              aria-current={item.id === thread.id ? 'true' : undefined}
              onClick={() => selectThread(item.id)}
            >
              <strong>{item.title}</strong>
              <span>{item.preview}</span>
            </button>
          ))}
        </div>
      </section>

      <section className="guide-panel" aria-label="Conversación">
        <div className="guide-panel-head">
          <div>
            <h1>Consejero</h1>
            <p>{thread.title}</p>
          </div>
          <button className="guide-btn guide-history-toggle" type="button" onClick={() => setHistoryOpen((open) => !open)}>
            Chats
          </button>
          <span className="guide-reading">{reading}</span>
        </div>
        <div className="guide-messages" ref={messagesRef} aria-live="polite">
          {thread.messages.map((message, index) => (
            <div key={`${message.role}-${index}`} className={`guide-bubble ${message.role}`}>
              {message.text}
            </div>
          ))}
          {pendingId === thread.id && (
            <div className="guide-bubble assistant pending">Pensando…</div>
          )}
        </div>
        <form className="guide-composer" onSubmit={sendMessage}>
          <div className="guide-chips">
            {PROMPTS.map((prompt) => (
              <button
                key={prompt}
                className="guide-chip"
                type="button"
                disabled={pending}
                onClick={() => setDraft(prompt)}
              >
                {prompt}
              </button>
            ))}
          </div>
          <div className="guide-composer-row">
            <input
              value={draft}
              maxLength={MAX_MESSAGE_LENGTH}
              disabled={pending}
              onChange={(event) => setDraft(event.target.value)}
              placeholder="Escribe al consejero…"
              aria-label="Mensaje para el consejero"
            />
            <button className="guide-send" type="submit" aria-label="Enviar" disabled={pending || !draft.trim()}>↑</button>
          </div>
        </form>
      </section>
    </main>
  )
}
