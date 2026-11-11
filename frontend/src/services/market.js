import { getJson, requestJson } from './http.js'

const marketUrl = '/api/players/me/market'

export function getMarket() {
  return getJson(marketUrl)
}

export function buyMarketItem(catalogId) {
  return requestJson(`${marketUrl}/purchases`, {
    method: 'POST',
    body: { catalogId },
  })
}

export function setItemEquipped(itemId, equipped) {
  return requestJson(`${marketUrl}/inventory/${itemId}/equipment`, {
    method: 'PUT',
    body: { equipped },
  })
}

export function sellMarketItem(itemId) {
  return requestJson(`${marketUrl}/inventory/${itemId}/sales`, { method: 'POST' })
}

export function upgradeMarketItem(itemId) {
  return requestJson(`${marketUrl}/inventory/${itemId}/upgrades`, { method: 'POST' })
}
