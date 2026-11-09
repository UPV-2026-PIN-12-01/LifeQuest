import { requestJson } from './http.js'

// Not cached: requests and friends change from the other player's side too.
export function getFriends() {
  return requestJson('/api/friends')
}

export function sendFriendRequest(playerName) {
  return requestJson('/api/friends/requests', { method: 'POST', body: { playerName } })
}

export function acceptFriendRequest(requestId) {
  return requestJson(`/api/friends/requests/${requestId}/accept`, { method: 'POST' })
}

// Rejects an incoming request or cancels one you sent.
export function deleteFriendRequest(requestId) {
  return requestJson(`/api/friends/requests/${requestId}`, { method: 'DELETE' })
}

export function getFriendProfile(friendUserId) {
  return requestJson(`/api/friends/${friendUserId}`)
}

export function removeFriend(friendUserId) {
  return requestJson(`/api/friends/${friendUserId}`, { method: 'DELETE' })
}

export function getFriendFeed() {
  return requestJson('/api/friends/feed')
}
