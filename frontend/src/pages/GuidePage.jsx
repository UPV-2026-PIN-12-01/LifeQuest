import { useEffect, useRef, useState } from 'react'
import ReactMarkdown from 'react-markdown'
import { Link, useNavigate, useOutletContext } from 'react-router-dom'
import { confirmChatProposal, discardChatProposal, listChatThreads, loadChatThread, sendChatMessage } from '../services/chat.js'
import { getTasksForWeek } from '../services/tasks.js'
import { startOfWeek, toIsoDate, weekRangeLabel } from '../utils/week.js'
import '../styles/guide.css'

const PROMPTS = ['Revisa mi semana', 'Qué hago hoy', 'Propón un desafío']
const MAX_MESSAGE_LENGTH = 2000
const GREETING = 'Dime qué quieres revisar. Puedo mirar tu semana, tus misiones de hoy y tus desafíos activos.'

function newThread(id) {
  return {
    id,
    local: true,
    loaded: true,
    title: 'Chat nuevo',
    preview: 'Sin mensajes todavía',
    messages: [{ role: 'assistant', text: GREETING }],
  }
}

function threadFromSummary(item) {
  return {
    id: item.id,
    local: false,
    loaded: false,
    title: item.title,
    preview: item.preview ?? '',
    messages: [],
  }
}

function threadFromDetail(detail) {
  const messages = (Array.isArray(detail.messages) ? detail.messages : []).map((message) => ({
    role: message.role,
    text: message.text,
    proposals: Array.isArray(message.proposals) ? message.proposals : [],
  }))
  const last = messages[messages.length - 1]
  return {
    id: detail.id,
    local: false,
    loaded: true,
    title: detail.title,
    preview: typeof last?.text === 'string' ? last.text.slice(0, 80) : '',
    messages,
  }
}

function plainPreview(text) {
  return String(text ?? '').replace(/[*_`#>]/g, '').replace(/\s+/g, ' ').trim()
}

function ProposalCard({ proposal, busy, onConfirm, onDiscard }) {
  const created = proposal.status === 'created'
  const kind = proposal.kind === 'challenge' ? 'challenge' : 'task'
  return (
    <article className={`guide-proposal${created ? ' done' : ''}`}>
      <header>
        <span className={`guide-kind ${kind}`}>{kind === 'challenge' ? 'Desafío' : 'Misión'}</span>
      </header>
      <h3>{proposal.title}</h3>
      {proposal.whenLabel ? <p className="meta">{proposal.whenLabel}</p> : null}
      {proposal.why ? <p className="why">{proposal.why}</p> : null}
      {proposal.status === 'pending' && (
        <div className="guide-actions">
          <button className="guide-btn-primary" type="button" disabled={busy} onClick={() => onConfirm(proposal.id)}>Crear</button>
          <button className="guide-btn-ghost" type="button" disabled={busy} onClick={() => onDiscard(proposal.id)}>Descartar</button>
        </div>
      )}
      {created && (
        <p className="guide-done-label">
          Creada
          {' · '}
          <Link to={kind === 'challenge' ? '/challenges' : '/home'}>
            {kind === 'challenge' ? 'Ver desafíos' : 'Ver misiones'}
          </Link>
        </p>
      )}
      {proposal.status === 'discarded' && <p className="meta">Descartada</p>}
    </article>
  )
}

function MarkdownText({ text }) {
  return (
    <div className="guide-markdown">
      <ReactMarkdown
        components={{
          a: ({ href, children }) => (
            <a href={href} target="_blank" rel="noreferrer">{children}</a>
          ),
        }}
      >
        {text}
      </ReactMarkdown>
    </div>
  )
}

function chatErrorText(error) {
  if (error?.status === 0) return 'No hay conexión. Inténtalo de nuevo.'
  if (error?.message === 'message must be at most 2000 characters') {
    return 'El mensaje puede tener como máximo 2000 caracteres.'
  }
  if (error?.status === 404) return 'No se encontró el chat.'
  if (error?.status === 502 || error?.message === 'Chat is unavailable') {
    return 'El consejero no está disponible ahora. Inténtalo de nuevo.'
  }
  return 'No se pudo enviar el mensaje.'
}

function loadErrorText(error) {
  if (error?.status === 0) return 'No hay conexión. Inténtalo de nuevo.'
  if (error?.status === 404) return 'No se encontró el chat.'
  return 'No se pudo cargar el chat.'
}

export default function GuidePage() {
  const { player } = useOutletContext()
  const navigate = useNavigate()
  const [threads, setThreads] = useState([])
  const [activeId, setActiveId] = useState(null)
  const [ready, setReady] = useState(false)
  const [draft, setDraft] = useState('')
  const [historyOpen, setHistoryOpen] = useState(false)
  const [taskCount, setTaskCount] = useState(null)
  const [pendingId, setPendingId] = useState(null)
  const [proposalBusyId, setProposalBusyId] = useState(null)
  const [proposalError, setProposalError] = useState('')
  const messagesRef = useRef(null)
  const localSeq = useRef(0)
  const weekStart = startOfWeek(new Date())
  const weekKey = toIsoDate(weekStart)
  const thread = threads.find((item) => item.id === activeId) ?? null
  const pending = pendingId !== null
  const threadLoading = Boolean(thread && !thread.local && !thread.loaded)

  function markLoadFailed(id, error) {
    setThreads((current) => current.map((entry) => (
      entry.id === id
        ? { ...entry, loaded: true, messages: [{ role: 'error', text: loadErrorText(error) }] }
        : entry
    )))
  }

  useEffect(() => {
    let cancelled = false
    listChatThreads()
      .then(async (list) => {
        if (cancelled) return
        const saved = (Array.isArray(list) ? list : []).map(threadFromSummary)
        if (saved.length === 0) {
          const created = newThread(`local-${localSeq.current + 1}`)
          localSeq.current += 1
          setThreads([created])
          setActiveId(created.id)
          return
        }
        setThreads(saved)
        setActiveId(saved[0].id)
        try {
          const detail = await loadChatThread(saved[0].id)
          if (cancelled) return
          setThreads((current) => current.map((item) => (
            item.id === saved[0].id ? threadFromDetail(detail) : item
          )))
        } catch (error) {
          if (cancelled) return
          if (error?.status === 401) {
            navigate('/login', { replace: true })
            return
          }
          markLoadFailed(saved[0].id, error)
        }
      })
      .catch((error) => {
        if (cancelled) return
        if (error?.status === 401) {
          navigate('/login', { replace: true })
          return
        }
        const created = newThread(`local-${localSeq.current + 1}`)
        localSeq.current += 1
        setThreads([created])
        setActiveId(created.id)
      })
      .finally(() => {
        if (!cancelled) setReady(true)
      })
    return () => {
      cancelled = true
    }
  }, [navigate])

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
    const item = threads.find((entry) => entry.id === id)
    if (!item || item.local || item.loaded) return
    loadChatThread(id)
      .then((detail) => {
        setThreads((current) => current.map((entry) => (
          entry.id === id ? threadFromDetail(detail) : entry
        )))
      })
      .catch((error) => {
        if (error?.status === 401) {
          navigate('/login', { replace: true })
          return
        }
        markLoadFailed(id, error)
      })
  }

  function startChat() {
    if (pending || !ready) return
    localSeq.current += 1
    const created = newThread(`local-${localSeq.current}`)
    setThreads((current) => [created, ...current])
    setActiveId(created.id)
    setDraft('')
    setHistoryOpen(false)
  }

  function placeSaved(sentFrom, detail) {
    setThreads((current) => {
      const locals = current.filter((item) => item.local && item.id !== sentFrom)
      const others = current.filter((item) => !item.local && item.id !== detail.id && item.id !== sentFrom)
      return [...locals, threadFromDetail(detail), ...others]
    })
    setActiveId((current) => (current === sentFrom ? detail.id : current))
  }

  async function settleProposal(proposalId, request) {
    if (!thread || thread.local || proposalBusyId != null) return
    setProposalBusyId(proposalId)
    setProposalError('')
    try {
      await request(proposalId)
      const detail = await loadChatThread(thread.id)
      setThreads((current) => current.map((item) => (
        item.id === thread.id ? threadFromDetail(detail) : item
      )))
    } catch (error) {
      if (error?.status === 401) {
        navigate('/login', { replace: true })
        return
      }
      setProposalError(error?.message || 'No se pudo actualizar la propuesta.')
    } finally {
      setProposalBusyId(null)
    }
  }

  async function sendMessage(event) {
    event.preventDefault()
    const text = draft.trim()
    if (!text || pending || !thread || threadLoading) return
    const sentFrom = thread.id
    const serverThreadId = thread.local ? undefined : thread.id
    const snapshot = thread
    setDraft('')
    setThreads((current) => current.map((item) => {
      if (item.id !== sentFrom) return item
      return {
        ...item,
        title: item.local ? text.slice(0, 32) : item.title,
        preview: text,
        messages: [...item.messages, { role: 'user', text }],
      }
    }))
    setPendingId(sentFrom)
    let payload
    try {
      payload = await sendChatMessage(text, serverThreadId)
    } catch (error) {
      if (error?.status === 401) {
        navigate('/login', { replace: true })
        setPendingId(null)
        return
      }
      setThreads((current) => current.map((item) => {
        if (item.id !== sentFrom) return item
        const messages = item.messages.slice()
        const last = messages[messages.length - 1]
        if (last?.role === 'user' && last.text === text) messages.pop()
        messages.push({ role: 'error', text: chatErrorText(error) })
        return {
          ...item,
          title: snapshot.title,
          preview: snapshot.preview,
          messages,
        }
      }))
      setDraft((current) => current || text)
      setPendingId(null)
      return
    }
    const reply = typeof payload?.reply === 'string' ? payload.reply.trim() : ''
    const threadId = payload?.threadId
    if (!reply || threadId == null) {
      setThreads((current) => current.map((item) => {
        if (item.id !== sentFrom) return item
        const messages = item.messages.slice()
        const last = messages[messages.length - 1]
        if (last?.role === 'user' && last.text === text) messages.pop()
        messages.push({ role: 'error', text: chatErrorText({ status: 502, message: 'Chat is unavailable' }) })
        return { ...item, title: snapshot.title, preview: snapshot.preview, messages }
      }))
      setDraft((current) => current || text)
      setPendingId(null)
      return
    }
    try {
      placeSaved(sentFrom, await loadChatThread(threadId))
    } catch (error) {
      if (error?.status === 401) {
        navigate('/login', { replace: true })
      } else {
        const prior = snapshot.local
          ? []
          : snapshot.messages.filter((item) => item.role === 'user' || item.role === 'assistant')
        placeSaved(sentFrom, {
          id: threadId,
          title: snapshot.local ? text.slice(0, 32) : snapshot.title,
          messages: [...prior, { role: 'user', text }, { role: 'assistant', text: reply }],
        })
      }
    } finally {
      setPendingId(null)
    }
  }

  return (
    <main className="guide-page">
      <section className={`guide-panel guide-history${historyOpen ? ' open' : ''}`} aria-label="Historial de chats">
        <div className="guide-panel-head"><h2>Chats</h2></div>
        <button className="guide-new-chat" type="button" onClick={startChat} disabled={pending || !ready}>+ Nuevo chat</button>
        <div className="guide-history-list">
          {threads.map((item) => (
            <button
              key={item.id}
              type="button"
              className={`guide-thread${item.id === thread?.id ? ' active' : ''}`}
              aria-current={item.id === thread?.id ? 'true' : undefined}
              onClick={() => selectThread(item.id)}
            >
              <strong>{item.title}</strong>
              <span>{plainPreview(item.preview)}</span>
            </button>
          ))}
        </div>
      </section>

      <section className="guide-panel" aria-label="Conversación">
        <div className="guide-panel-head">
          <div>
            <h1>Consejero</h1>
            <p>{thread?.title ?? 'Chat nuevo'}</p>
          </div>
          <button className="guide-btn guide-history-toggle" type="button" onClick={() => setHistoryOpen((open) => !open)}>
            Chats
          </button>
          <span className="guide-reading">{reading}</span>
        </div>
        <div className="guide-messages" ref={messagesRef} aria-live="polite">
          {threadLoading && (
            <div className="guide-bubble assistant pending">Cargando…</div>
          )}
          {(thread?.messages ?? []).map((message, index) => (
            <div key={`${message.role}-${index}`}>
              <div className={`guide-bubble ${message.role}`}>
                {message.role === 'assistant'
                  ? <MarkdownText text={message.text} />
                  : message.text}
              </div>
              {message.role === 'assistant' && (message.proposals ?? []).map((proposal) => (
                <ProposalCard
                  key={proposal.id}
                  proposal={proposal}
                  busy={proposalBusyId === proposal.id}
                  onConfirm={(id) => settleProposal(id, confirmChatProposal)}
                  onDiscard={(id) => settleProposal(id, discardChatProposal)}
                />
              ))}
            </div>
          ))}
          {proposalError && (
            <div className="guide-bubble error">{proposalError}</div>
          )}
          {pendingId === thread?.id && (
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
                disabled={pending || threadLoading || !thread}
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
              disabled={pending || threadLoading || !thread}
              onChange={(event) => setDraft(event.target.value)}
              placeholder="Escribe al consejero…"
              aria-label="Mensaje para el consejero"
            />
            <button className="guide-send" type="submit" aria-label="Enviar" disabled={pending || threadLoading || !thread || !draft.trim()}>↑</button>
          </div>
        </form>
      </section>
    </main>
  )
}
