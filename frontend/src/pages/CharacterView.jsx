import { useEffect, useState } from "react"
import {
  SHOP_ITEMS,
  INITIAL_STATS,
  INITIAL_INVENTORY,
} from "../data"
import warriorImg from "../assets/warrior1.png"
import archerImg from "../assets/archer1.png"
import magicianImg from "../assets/magician1.png"
import { getJson } from "../services/http.js"
import CharacterInventoryView from "../components/character/CharacterInventoryView"
import CharacterOverviewView from "../components/character/CharacterOverviewView"

const PLAYER_CLASS_META = {
  knight: { name: "Guerrero", image: warriorImg },
  archer: { name: "Arquero", image: archerImg },
  magician: { name: "Mago", image: magicianImg },
}

export default function CharacterView({
  stats,
  playerName = "Aventurero",
  avatar = "🧙",
  inventory,
  initialTab = "character",
}) {
  const [loadedStats, setLoadedStats] = useState(stats ?? null)
  const [statsError, setStatsError] = useState("")
  const [items, setItems] = useState(inventory ?? INITIAL_INVENTORY)
  const [tab, setTab] = useState(initialTab)
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

  const currentStats = loadedStats ?? INITIAL_STATS
  const displayName = loadedStats?.playerName ?? playerName
  const playerClass =
    PLAYER_CLASS_META[loadedStats?.playerClass] ?? PLAYER_CLASS_META.knight

  function showFeedback(message) {
    setFeedback(message)
    window.setTimeout(() => setFeedback(null), 2200)
  }

  function buy(item) {
    if (currentStats.gold < item.buyPrice) {
      showFeedback("No tienes suficiente oro.")
      return
    }
    setLoadedStats((currentStats) => ({
      ...(currentStats ?? INITIAL_STATS),
      gold: (currentStats ?? INITIAL_STATS).gold - item.buyPrice,
    }))
    setItems((currentItems) => {
      const existingPotion =
        item.category === "Poción"
          ? currentItems.find(
              (ownedItem) => ownedItem.catalogId === item.catalogId,
            )
          : null
      if (existingPotion) {
        return currentItems.map((ownedItem) =>
          ownedItem.id === existingPotion.id
            ? { ...ownedItem, quantity: (ownedItem.quantity ?? 1) + 1 }
            : ownedItem,
        )
      }
      return [
        ...currentItems,
        { ...item, id: `${item.id}-${crypto.randomUUID()}`, equipped: false },
      ]
    })
    showFeedback(`¡${item.name} añadido al inventario!`)
  }

  function sell(id) {
    const item = items.find((ownedItem) => ownedItem.id === id)
    if (!item) return

    const salePrice = Math.floor(item.buyPrice * 0.7)
    setItems((currentItems) =>
      currentItems.flatMap((ownedItem) => {
        if (ownedItem.id !== id) return [ownedItem]
        if (ownedItem.quantity > 1) {
          return [{ ...ownedItem, quantity: ownedItem.quantity - 1 }]
        }
        return []
      }),
    )
    setLoadedStats((currentStats) => ({
      ...(currentStats ?? INITIAL_STATS),
      gold: (currentStats ?? INITIAL_STATS).gold + salePrice,
    }))
    showFeedback(`¡${item.name} vendido por ${salePrice} oro!`)
  }

  function toggleEquip(id) {
    const item = items.find((ownedItem) => ownedItem.id === id)
    if (!item || item.category === "Poción") return

    setItems((currentItems) =>
      currentItems.map((ownedItem) => {
        if (ownedItem.id === id) {
          return { ...ownedItem, equipped: !ownedItem.equipped }
        }
        if (ownedItem.category === item.category) {
          return { ...ownedItem, equipped: false }
        }
        return ownedItem
      }),
    )
  }

  function upgrade(id, cost) {
    if (currentStats.gold < cost) {
      showFeedback("No tienes suficiente oro.")
      return
    }
    setItems((currentItems) =>
      currentItems.map((item) => {
        if (item.id !== id) return item
        return {
          ...item,
          level: item.level + 1,
          stats: item.stats.map((stat) =>
            stat.replace(/(\d+)/, (value) => String(Number(value) + 1)),
          ),
        }
      }),
    )
    setLoadedStats((currentStats) => ({
      ...(currentStats ?? INITIAL_STATS),
      gold: (currentStats ?? INITIAL_STATS).gold - cost,
    }))
    showFeedback("¡Objeto mejorado! Sus estadísticas han aumentado.")
  }

  return (
    <div className="p-4 lg:p-6 max-w-6xl mx-auto">
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
            className="px-4 py-2 rounded-xl text-sm font-semibold whitespace-nowrap transition"
            style={{
              background:
                tab === key ? "var(--color-primary)" : "transparent",
              color: tab === key ? "white" : "#64748b",
            }}
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

      {tab === "character" ? (
        <CharacterOverviewView
          stats={currentStats}
          displayName={displayName}
          playerClass={playerClass}
          avatar={avatar}
        />
      ) : (
        <CharacterInventoryView
          tab={tab}
          stats={currentStats}
          inventory={items}
          shopItems={SHOP_ITEMS}
          onBuy={buy}
          onSell={sell}
          onToggleEquip={toggleEquip}
          onUpgrade={upgrade}
        />
      )}
    </div>
  )
}
