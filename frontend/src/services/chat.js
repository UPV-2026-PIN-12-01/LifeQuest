import { requestJson } from './http.js'

export function listChatThreads() {
  return requestJson('/api/chat/threads')
}

export function loadChatThread(threadId) {
  return requestJson(`/api/chat/threads/${threadId}`)
}

// A new chat omits threadId. The server id comes back with the first reply.
export function sendChatMessage(message, threadId) {
  const body = threadId == null ? { message } : { message, threadId }
  return requestJson('/api/chat/messages', { method: 'POST', body })
}
