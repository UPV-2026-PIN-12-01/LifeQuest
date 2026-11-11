import { useEffect, useState } from "react"
import { INITIAL_STATS } from "../data"
import warriorImg from "../assets/warrior1.png"
import archerImg from "../assets/archer1.png"
import magicianImg from "../assets/magician1.png"
import { getJson } from "../services/http.js"
import deafultImg from "../assets/placeholder.png"
import {
  buyMarketItem,
  getMarket,
  sellMarketItem,
  setItemEquipped,
  upgradeMarketItem,
} from "../services/market.js"
import CharacterInventoryView from "../components/character/CharacterInventoryView"
import CharacterOverviewView from "../components/character/CharacterOverviewView"
import "../styles/character.css"

const PLAYER_CLASS_META = {
  knight: { name: "Guerrero", image: warriorImg },
  archer: { name: "Arquero", image: archerImg },
  magician: { name: "Mago", image: magicianImg },
  default: { name: "Aventurero", image: deafultImg }
}

export default function CharacterView({
  stats,
  playerName = "Aventurero",
  avatar = "🧙",
  initialTab = "character",
}) {
  const [loadedStats, setLoadedStats] = useState(stats ?? null)
  const [statsError, setStatsError] = useState("")
  const [market, setMarket] = useState(null)
  const [marketLoading, setMarketLoading] = useState(true)
  const [marketError, setMarketError] = useState("")
  const [pendingAction, setPendingAction] = useState(false)
  const [tab, setTab] = useState(initialTab)
  const [filter, setFilter] = useState("Todos")
  const [feedback, setFeedback] = useState(null)

  useEffect(() => {
    let cancelled = false

    getJson("/api/character/stats")
      .then((characterStats) => {
        if (!cancelled) {
          setLoadedStats((currentStats) => ({
            ...currentStats,
            ...characterStats,
            xp: characterStats.xpCurrent,
            xpToNext: 2 * characterStats.level + 11,
            knowledge: characterStats.wisdom,
          }))
        }
      })
      .catch((error) => {
        if (!cancelled) setStatsError(error.message)
      })

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false

    getMarket()
      .then((marketData) => {
        if (!cancelled) {
          setMarket(marketData)
          setLoadedStats((currentStats) => ({
            ...INITIAL_STATS,
            ...currentStats,
            gold: marketData.gold,
          }))
        }
      })
      .catch((error) => {
        if (!cancelled) setMarketError(error.message)
      })
      .finally(() => {
        if (!cancelled) setMarketLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const currentStats = loadedStats ?? INITIAL_STATS
  const displayName = loadedStats?.playerName ?? playerName
  const playerClass =
    PLAYER_CLASS_META[loadedStats?.playerClass] ?? PLAYER_CLASS_META.default
  const objectStats = market?.objectStats ?? {}

  function showFeedback(message) {
    setFeedback(message)
    window.setTimeout(() => setFeedback(null), 2200)
  }

  async function applyMarketAction(action, successMessage) {
    setPendingAction(true)
    setMarketError("")
    try {
      const updatedMarket = await action()
      setMarket(updatedMarket)
      setLoadedStats((currentStats) => ({
        ...(currentStats ?? INITIAL_STATS),
        gold: updatedMarket.gold,
      }))
      showFeedback(successMessage)
      return true
    } catch (error) {
      setMarketError(
        error.status === 409
          ? error.message
          : error.message || "No se pudo completar la acción. Inténtalo de nuevo.",
      )
      return false
    } finally {
      setPendingAction(false)
    }
  }

  function buy(item) {
    return applyMarketAction(
      () => buyMarketItem(item.catalogId),
      `¡${item.name} añadido al inventario!`,
    )
  }

  function sell(id) {
    const item = market?.inventory.find((ownedItem) => ownedItem.id === id)
    if (!item) return

    const salePrice = Math.floor(item.buyPrice * 0.7)
    return applyMarketAction(
      () => sellMarketItem(id),
      `¡${item.name} vendido por ${salePrice} oro!`,
    )
  }

  function toggleEquip(id) {
    const item = market?.inventory.find((ownedItem) => ownedItem.id === id)
    if (!item || item.category === "Poción") return
    return applyMarketAction(
      () => setItemEquipped(id, !item.equipped),
      item.equipped ? `${item.name} desequipado.` : `¡${item.name} equipado!`,
    )
  }

  function upgrade(id) {
    return applyMarketAction(
      () => upgradeMarketItem(id),
      "¡Objeto mejorado! Sus estadísticas han aumentado.",
    )
  }

  return (
    <div className="character-view p-4 lg:p-6 max-w-6xl mx-auto">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-5">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Mi Personaje</h1>
          <p className="text-sm text-slate-500">
            Gestiona tu progreso, equipamiento y recursos.
          </p>
        </div>
        <div className="flex items-center gap-2 px-3 py-2 rounded-xl bg-amber-50 border border-amber-100 text-amber-600 font-bold">
          💰 {currentStats.gold} oro
        </div>
      </div>

      <div className="flex gap-2 bg-white rounded-2xl p-1.5 border border-slate-100 w-full sm:w-fit mb-5 overflow-x-auto">
        {[
          ["character", "⚔️ Personaje"],
          ["inventory", "🎒 Inventario"],
          ["shop", "🏪 Tienda"],
        ].map(([key, label]) => (
          <button
            key={key}
            onClick={() => setTab(key)}
            className={`character-tab-button px-4 py-2 rounded-xl text-sm font-semibold whitespace-nowrap transition${tab === key ? " is-active" : ""}`}
          >
            {label}
          </button>
        ))}
      </div>

      {feedback && (
        <div className="fixed top-20 right-4 z-50 px-4 py-3 rounded-2xl bg-emerald-600 text-white text-sm font-semibold shadow-xl animate-reward">
          {feedback}
        </div>
      )}

      {statsError && (
        <div
          role="alert"
          className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
        >
          {statsError}
        </div>
      )}

      {marketError && (
        <div
          role="alert"
          className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700"
        >
          {marketError}
        </div>
      )}
      
      {/*Character Overview or Inventory View*/}
      {tab === "character" ? (
        <CharacterOverviewView
          stats={currentStats}
          objectStats={objectStats}
          avatar={avatar}
          displayName={displayName}
          playerClass={playerClass}
        />
      ) : (
        <CharacterInventoryView
          tab={tab}
          market={market}
          marketLoading={marketLoading}
          gold={currentStats.gold}
          filter={filter}
          setFilter={setFilter}
          onBuy={buy}
          onSell={sell}
          onToggleEquip={toggleEquip}
          onUpgrade={upgrade}
          busy={pendingAction}
        />
      )}
    </div>
  )
}
