import { useEffect, useId, useState } from 'react'
import AvatarDisplay from './AvatarDisplay.jsx'
import { searchPlayers } from '../services/friends.js'
import '../styles/player-search.css'

const MIN_LENGTH = 2
const DEBOUNCE_MS = 250

const RELATION_LABELS = {
  FRIEND: 'Ya es tu amigo',
  OUTGOING: 'Solicitud enviada',
  INCOMING: 'Te ha enviado una solicitud',
}

// Players you can't send a request to are shown but can't be picked.
const isPickable = (suggestion) => suggestion.relation === 'NONE' || suggestion.relation === 'INCOMING'

// Text input with a list of players whose name contains what you type (combobox pattern).
// Picking one fills the input with their exact name; the parent's form still sends the request.
export default function PlayerSearchInput({ id, value, onChange, placeholder, maxLength }) {
  const listId = useId()
  // Last answer from the server and the text it was for.
  const [result, setResult] = useState({ query: '', suggestions: [], failed: false })
  const [open, setOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(-1)
  // Name just chosen from the list: no need to search it again until the user edits it.
  const [pickedName, setPickedName] = useState(null)

  const query = value.trim()
  const searching = query.length >= MIN_LENGTH && query !== pickedName
  const upToDate = result.query === query
  // While a new search runs, keep showing the previous suggestions instead of flickering.
  const suggestions = searching ? result.suggestions : []
  const status = !searching ? 'idle' : !upToDate ? 'loading' : result.failed ? 'error' : 'done'

  useEffect(() => {
    if (!searching) return undefined
    let cancelled = false
    const timer = setTimeout(() => {
      searchPlayers(query)
        .then((found) => {
          if (cancelled) return
          setResult({ query, suggestions: found ?? [], failed: false })
          setActiveIndex(-1)
        })
        .catch(() => !cancelled && setResult({ query, suggestions: [], failed: true }))
    }, DEBOUNCE_MS)
    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [query, searching])

  function pick(suggestion) {
    if (!isPickable(suggestion)) return
    setPickedName(suggestion.player.playerName)
    onChange(suggestion.player.playerName)
    setOpen(false)
    setActiveIndex(-1)
  }

  function move(step) {
    if (suggestions.length === 0) return
    setOpen(true)
    setActiveIndex((current) => {
      let next = current
      for (let i = 0; i < suggestions.length; i += 1) {
        next = (next + step + suggestions.length) % suggestions.length
        if (isPickable(suggestions[next])) return next
      }
      return current
    })
  }

  function onKeyDown(event) {
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      move(1)
    } else if (event.key === 'ArrowUp') {
      event.preventDefault()
      move(-1)
    } else if (event.key === 'Enter' && open && activeIndex >= 0) {
      event.preventDefault()
      pick(suggestions[activeIndex])
    } else if (event.key === 'Escape' && open) {
      event.preventDefault()
      setOpen(false)
    }
  }

  const showList = open && searching
  const optionId = (index) => `${listId}-option-${index}`

  return (
    <div className="player-search">
      <input
        id={id}
        value={value}
        onChange={(event) => {
          setPickedName(null)
          setOpen(true)
          onChange(event.target.value)
        }}
        onKeyDown={onKeyDown}
        onFocus={() => setOpen(true)}
        onBlur={() => setOpen(false)}
        placeholder={placeholder}
        maxLength={maxLength}
        autoComplete="off"
        role="combobox"
        aria-autocomplete="list"
        aria-expanded={showList}
        aria-controls={listId}
        aria-activedescendant={showList && activeIndex >= 0 ? optionId(activeIndex) : undefined}
      />
      {showList && (
        <ul id={listId} className="player-search-list" role="listbox" aria-label="Jugadores sugeridos">
          {status === 'loading' && suggestions.length === 0 && <li className="player-search-note">Buscando…</li>}
          {status === 'error' && <li className="player-search-note">No se pudieron cargar sugerencias.</li>}
          {status === 'done' && suggestions.length === 0 && (
            <li className="player-search-note">Ningún jugador coincide con «{query}».</li>
          )}
          {suggestions.map((suggestion, index) => {
            const { player, relation } = suggestion
            const pickable = isPickable(suggestion)
            return (
              <li
                key={player.userId}
                id={optionId(index)}
                role="option"
                aria-selected={index === activeIndex}
                aria-disabled={!pickable}
                className={`player-search-option${index === activeIndex ? ' is-active' : ''}${pickable ? '' : ' is-disabled'}`}
                // mousedown instead of click so the input's blur doesn't close the list first.
                onMouseDown={(event) => {
                  event.preventDefault()
                  pick(suggestion)
                }}
                onMouseEnter={() => pickable && setActiveIndex(index)}
              >
                <AvatarDisplay avatar={player.profileUrl || player.userIcon || '⚔️'} size={32} />
                <span className="player-search-name">
                  <Highlight text={player.playerName} query={query} />
                  <small>{player.playerClass} · Nivel {player.level}</small>
                </span>
                {RELATION_LABELS[relation] && <span className="player-search-relation">{RELATION_LABELS[relation]}</span>}
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}

// Bolds the first place where the name contains the query, ignoring case.
function Highlight({ text, query }) {
  const start = text.toLowerCase().indexOf(query.toLowerCase())
  if (start < 0) return <strong>{text}</strong>
  return (
    <strong>
      {text.slice(0, start)}
      <mark>{text.slice(start, start + query.length)}</mark>
      {text.slice(start + query.length)}
    </strong>
  )
}
