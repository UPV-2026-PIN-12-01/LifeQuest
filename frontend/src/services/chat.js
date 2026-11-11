import { invalidateCachedData } from './dataCache.js'
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

// Crear writes a task or a challenge. Drop the same lists those create calls already drop.
export async function confirmChatProposal(proposalId) {
  const proposal = await requestJson(`/api/chat/proposals/${proposalId}/confirm`, { method: 'POST' })
  if (proposal?.kind === 'task') {
    invalidateCachedData('/api/players/me/tasks', '/api/players/me/daily-quests')
  } else if (proposal?.kind === 'challenge') {
    invalidateCachedData('/api/players/me/challenges', '/api/players/me', '/api/character/stats')
  }
  return proposal
}

export function discardChatProposal(proposalId) {
  return requestJson(`/api/chat/proposals/${proposalId}/discard`, { method: 'POST' })
}
