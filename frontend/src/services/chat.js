import { requestJson } from './http.js'

// One turn. The server does not receive earlier messages and does not store the thread.
export async function sendChatMessage(message) {
  return requestJson('/api/chat/messages', { method: 'POST', body: { message } })
}

// Kept while this tab stays open. A reload starts over; the server has no threads yet.
let chatSession = null

export function loadChatSession() {
  return chatSession
}

export function saveChatSession(session) {
  chatSession = session
}
