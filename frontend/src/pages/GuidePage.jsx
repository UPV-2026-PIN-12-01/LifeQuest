import { useEffect, useId, useRef, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { getTasksForWeek } from '../services/tasks.js'
import { startOfWeek, toIsoDate, weekRangeLabel } from '../utils/week.js'
import '../styles/guide.css'

const PROMPTS = ['Revisa mi semana', 'Qué hago hoy', 'Propón un desafío']

const INITIAL_PROPOSALS = {
  repaso: {
    id: 'repaso',
    kind: 'task',
    kindLabel: 'Tarea',
    xp: '+40 XP',
    title: 'Repaso de PIN',
    when: 'Viernes · 09:00–10:30',
    why: 'Sale del jueves, que ya tiene cuatro bloques seguidos.',
    status: 'pending',
  },
  estudio: {
    id: 'estudio',
    kind: 'challenge',
    kindLabel: 'Desafío',
    xp: '+60 XP',
    title: 'Estudio sin pausas',
    when: 'Sobre Estudio · jueves 18:00',
    why: 'Completa la tarea respetando el tiempo establecido.',
    status: 'pending',
  },
}

const INITIAL_THREADS = [
  {
    id: 'week',
    title: 'Organizar la semana',
    day: 'Hoy',
    preview: 'Mover Repaso y un desafío de Estudio',
    messages: [
      { role: 'assistant', text: 'Hola. Esta semana vas 3 de 8, con racha de 4 días. El jueves está cargado: Estudio, Entreno, Proyecto y Repaso seguidos.' },
      { role: 'user', text: '¿Puedes ayudarme a organizar mi semana?' },
      { role: 'assistant', text: 'Puedo aligerar el jueves. Estos dos cambios usan tareas que ya tienes. No se guardan hasta que pulses Crear.' },
      { role: 'proposal', id: 'repaso' },
      { role: 'proposal', id: 'estudio' },
    ],
  },
  {
    id: 'today',
    title: 'Qué hago hoy',
    day: 'Hoy',
    preview: 'Empieza por Entreno para cuidar la racha',
    messages: [
      { role: 'user', text: '¿Qué hago primero hoy?' },
      { role: 'assistant', text: 'Empieza por Entreno: es corta y mantiene la racha. Estudio puede esperar a la tarde, que es cuando sueles dejar las tareas largas.' },
    ],
  },
  {
    id: 'yesterday',
    title: 'Desafío de lectura',
    day: 'Ayer',
    preview: 'Desafío creado sobre Lectura',
    messages: [
      { role: 'user', text: 'Quiero un desafío de la lectura de ayer.' },
      { role: 'assistant', text: 'Listo. Convertí Lectura en un desafío de tiempo estricto. Ya está en Desafíos.' },
    ],
  },
]

function replyFor(text) {
  const lower = text.toLowerCase()
  if (lower.includes('desaf')) {
    return {
      text: 'Te propongo un desafío sobre Estudio del jueves. Confírmalo y pasa a Desafíos.',
      proposalId: 'estudio',
    }
  }
  if (lower.includes('semana') || lower.includes('tarea') || lower.includes('organizar')) {
    return {
      text: 'El jueves sigue lleno. Si mueves Repaso al viernes, el día queda en tres bloques.',
      proposalId: 'repaso',
    }
  }
  if (lower.includes('hoy')) {
    return { text: 'Hoy prioriza Entreno. Es la tarea corta que mantiene la racha de 4 días. Estudio puede ir a la tarde.' }
  }
  return { text: 'Puedo proponerte cambios sobre lo que ya tienes esta semana. Prueba con “Revisa mi semana” o “Propón un desafío”.' }
}

export default function GuidePage() {
  const { player } = useOutletContext()
  const [proposals, setProposals] = useState(INITIAL_PROPOSALS)
  const [threads, setThreads] = useState(INITIAL_THREADS)
  const [activeId, setActiveId] = useState('week')
  const [draft, setDraft] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [historyOpen, setHistoryOpen] = useState(false)
  const [taskCount, setTaskCount] = useState(null)
  const [threadSeq, setThreadSeq] = useState(1)
  const messagesRef = useRef(null)
  const weekStart = startOfWeek(new Date())
  const weekKey = toIsoDate(weekStart)
  const thread = threads.find((item) => item.id === activeId) ?? threads[0]

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
  }, [thread, proposals])

  const reading = [
    `Semana ${weekRangeLabel(weekStart)}`,
    taskCount == null ? null : `${taskCount} tareas`,
    `racha ${player.streakDays ?? 0}`,
  ].filter(Boolean).join(' · ')

  function selectThread(id) {
    setActiveId(id)
    setHistoryOpen(false)
  }

  function updateProposal(id, patch) {
    setProposals((current) => ({
      ...current,
      [id]: { ...current[id], ...patch },
    }))
  }

  function startChat() {
    const id = `new-${threadSeq + 1}`
    setThreadSeq((value) => value + 1)
    setThreads((current) => [
      {
        id,
        title: 'Chat nuevo',
        day: 'Hoy',
        preview: 'Sin mensajes todavía',
        messages: [
          { role: 'assistant', text: 'Dime qué quieres revisar. Puedo mirar tu semana, decirte por dónde empezar hoy o proponerte un desafío sobre una tarea que ya existe.' },
        ],
      },
      ...current,
    ])
    setActiveId(id)
    setHistoryOpen(false)
  }

  function sendMessage(event) {
    event.preventDefault()
    const text = draft.trim()
    if (!text) return
    const reply = replyFor(text)
    if (reply.proposalId) {
      setProposals((current) => (
        current[reply.proposalId].status === 'discarded'
          ? { ...current, [reply.proposalId]: { ...current[reply.proposalId], status: 'pending' } }
          : current
      ))
    }
    setThreads((current) => current.map((item) => {
      if (item.id !== activeId) return item
      const messages = [
        ...item.messages,
        { role: 'user', text },
        { role: 'assistant', text: reply.text },
      ]
      if (reply.proposalId) messages.push({ role: 'proposal', id: reply.proposalId })
      return {
        ...item,
        title: item.title === 'Chat nuevo' ? text.slice(0, 32) : item.title,
        preview: text,
        messages,
      }
    }))
    setDraft('')
  }

  return (
    <main className="guide-page">
      <section className={`guide-panel guide-history${historyOpen ? ' open' : ''}`} aria-label="Historial de chats">
        <div className="guide-panel-head"><h2>Chats</h2></div>
        <button className="guide-new-chat" type="button" onClick={startChat}>+ Nuevo chat</button>
        <div className="guide-history-list">
          {['Hoy', 'Ayer'].map((day) => {
            const items = threads.filter((item) => item.day === day)
            if (!items.length) return null
            return (
              <div key={day}>
                <h3>{day}</h3>
                {items.map((item) => (
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
            )
          })}
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
        <div className="guide-messages" ref={messagesRef}>
          {thread.messages.map((message, index) => {
            if (message.role === 'proposal') {
              const proposal = proposals[message.id]
              if (!proposal || proposal.status === 'discarded') return null
              return (
                <ProposalCard
                  key={`${message.id}-${index}`}
                  proposal={proposal}
                  onCreate={() => updateProposal(proposal.id, { status: 'created' })}
                  onDiscard={() => updateProposal(proposal.id, { status: 'discarded' })}
                  onEdit={() => setEditingId(proposal.id)}
                />
              )
            }
            return (
              <div key={`${message.role}-${index}`} className={`guide-bubble ${message.role}`}>
                {message.text}
              </div>
            )
          })}
        </div>
        <form className="guide-composer" onSubmit={sendMessage}>
          <div className="guide-chips">
            {PROMPTS.map((prompt) => (
              <button key={prompt} className="guide-chip" type="button" onClick={() => setDraft(prompt)}>
                {prompt}
              </button>
            ))}
          </div>
          <div className="guide-composer-row">
            <input
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
              placeholder="Escribe al consejero…"
              aria-label="Mensaje para el consejero"
            />
            <button className="guide-send" type="submit" aria-label="Enviar">↑</button>
          </div>
        </form>
      </section>

      {editingId && (
        <EditProposalDialog
          proposal={proposals[editingId]}
          onClose={() => setEditingId(null)}
          onSave={(patch) => {
            updateProposal(editingId, patch)
            setEditingId(null)
          }}
        />
      )}
    </main>
  )
}

function ProposalCard({ proposal, onCreate, onDiscard, onEdit }) {
  if (proposal.status === 'created') {
    const where = proposal.kind === 'challenge' ? 'Desafíos' : 'Inicio'
    return (
      <article className="guide-proposal done">
        <header>
          <span className={`guide-kind ${proposal.kind}`}>{proposal.kindLabel}</span>
          <span className="guide-xp">{proposal.xp}</span>
        </header>
        <h3>{proposal.title}</h3>
        <p className="meta">{proposal.when}</p>
        <p className="guide-done-label">Creada · ver en {where}</p>
      </article>
    )
  }

  return (
    <article className="guide-proposal">
      <header>
        <span className={`guide-kind ${proposal.kind}`}>{proposal.kindLabel}</span>
        <span className="guide-xp">{proposal.xp}</span>
      </header>
      <h3>{proposal.title}</h3>
      <p className="meta">{proposal.when}</p>
      <p className="why">{proposal.why}</p>
      <div className="guide-actions">
        <button className="guide-btn-primary" type="button" onClick={onCreate}>Crear</button>
        <button className="guide-btn" type="button" onClick={onEdit}>Editar</button>
        <button className="guide-btn-ghost" type="button" onClick={onDiscard}>Descartar</button>
      </div>
    </article>
  )
}

function EditProposalDialog({ proposal, onClose, onSave }) {
  const dialogRef = useRef(null)
  const titleId = useId()
  const whenId = useId()
  const whyId = useId()
  const [title, setTitle] = useState(proposal.title)
  const [when, setWhen] = useState(proposal.when)
  const [why, setWhy] = useState(proposal.why)

  useEffect(() => {
    const dialog = dialogRef.current
    if (dialog && !dialog.open) dialog.showModal()
    return () => {
      if (dialog?.open) dialog.close()
    }
  }, [])

  return (
    <dialog
      ref={dialogRef}
      className="guide-edit"
      aria-labelledby="guide-edit-title"
      onCancel={(event) => {
        event.preventDefault()
        onClose()
      }}
    >
      <form
        onSubmit={(event) => {
          event.preventDefault()
          onSave({ title: title.trim(), when: when.trim(), why: why.trim() })
        }}
      >
        <h2 id="guide-edit-title">{proposal.kind === 'challenge' ? 'Editar desafío' : 'Editar tarea'}</h2>
        <label className="guide-field" htmlFor={titleId}>
          Título
          <input id={titleId} value={title} onChange={(event) => setTitle(event.target.value)} required />
        </label>
        <label className="guide-field" htmlFor={whenId}>
          Cuándo
          <input id={whenId} value={when} onChange={(event) => setWhen(event.target.value)} required />
        </label>
        <label className="guide-field" htmlFor={whyId}>
          Detalle
          <textarea id={whyId} rows={3} value={why} onChange={(event) => setWhy(event.target.value)} />
        </label>
        <div className="guide-edit-actions">
          <button className="guide-btn" type="button" onClick={onClose}>Cancelar</button>
          <button className="guide-btn-primary" type="submit">Guardar</button>
        </div>
      </form>
    </dialog>
  )
}
