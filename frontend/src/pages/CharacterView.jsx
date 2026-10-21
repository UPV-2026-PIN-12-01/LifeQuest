import { useEffect, useState } from "react"
import {
  RARITY_META,
  SHOP_ITEMS,
  INITIAL_STATS,
  INITIAL_INVENTORY,
} from "../data"
import warriorImg from "../assets/warrior1.png"
import AvatarDisplay from "../components/AvatarDisplay"
import { getCharacterStats } from "../services/auth"
const STAT_META = [
  { key: "hp", label: "Vida", icon: "❤️", color: "#ef4444" },
  { key: "strength", label: "Fuerza", icon: "💪", color: "#f59e0b" },
  { key: "knowledge", label: "Conocimiento", icon: "🧠", color: "#6366f1" },
  { key: "speed", label: "Velocidad", icon: "⚡", color: "#0ea5e9" },
  { key: "stamina", label: "Estamina", icon: "🌿", color: "#10b981" },
]
const CATEGORY_FILTERS = [
  "Todos",
  "Arma",
  "Armadura",
  "Casco",
  "Botas",
  "Accesorio",
  "Poción",
]
function RarityBadge({ rarity }) {
  const meta = RARITY_META[rarity]
  return (
    <span
      className="text-xs px-2 py-0.5 rounded-full font-semibold"
      style={{ color: meta.color, background: meta.bg }}
    >
      {rarity}
    </span>
  )
}
function ItemCard({
  item,
  mode,
  gold,
  onBuy,
  onSell,
  onToggleEquip,
  onUpgrade,
}) {
  const [confirmSell, setConfirmSell] = useState(false)
  const upgradeCost = item.level * 55
  const salePrice = Math.floor(item.buyPrice * 0.7)
  const isPotion = item.category === "Poción"
  return (
    <div
      className="bg-white rounded-2xl border p-4 flex flex-col"
      style={{
        borderColor: item.equipped
          ? RARITY_META[item.rarity].border
          : "#e2e8f0",
      }}
    >
      <div className="flex items-start gap-3">
        <div
          className="w-12 h-12 rounded-2xl flex items-center justify-center text-2xl shrink-0"
          style={{ background: RARITY_META[item.rarity].bg }}
        >
          {item.icon}
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <p className="font-semibold text-sm text-slate-800 leading-tight">
              {item.name}
            </p>
            {item.quantity && item.quantity > 1 ? (
              <span className="text-xs font-bold text-indigo-600">
                ×{item.quantity}
              </span>
            ) : null}
          </div>
          <div className="flex flex-wrap items-center gap-1.5 mt-1.5">
            <RarityBadge rarity={item.rarity} />
            <span className="text-xs text-slate-400">
              Nv. {item.level} · {item.category}
            </span>
          </div>
        </div>
      </div>
      <div className="mt-3 rounded-xl bg-slate-50 p-2.5 min-h-12">
        {item.stats.map((stat) => (
          <p key={stat} className="text-xs font-medium text-slate-600">
            ↑ {stat}
          </p>
        ))}
      </div>

      {mode === "shop" ? (
        <div className="mt-auto pt-3 flex items-center justify-between gap-2">
          <div>
            <p className="font-bold text-amber-500">💰 {item.buyPrice}</p>
            <p className="text-xs text-slate-400">Oro</p>
          </div>
          <button
            onClick={() => onBuy(item)}
            disabled={gold < item.buyPrice}
            className="px-4 py-2 rounded-xl text-xs font-semibold text-white disabled:opacity-40 disabled:cursor-not-allowed"
            style={{ background: "var(--color-primary)" }}
          >
            {gold < item.buyPrice ? "Oro insuficiente" : "Comprar"}
          </button>
        </div>
      ) : (
        <>
          <div className="flex justify-between text-xs text-slate-400 mt-3">
            <span>Compra: {item.buyPrice} oro</span>
            <span>Venta: {salePrice} oro</span>
          </div>
          <div className="mt-3 flex flex-wrap gap-2">
            {!isPotion && (
              <>
                <button
                  onClick={() => onToggleEquip(item.id)}
                  className="flex-1 px-3 py-2 rounded-xl text-xs font-semibold"
                  style={{
                    background: item.equipped ? "#e2e8f0" : "#e0e7ff",
                    color: item.equipped ? "#475569" : "#4f46e5",
                  }}
                >
                  {item.equipped ? "Desequipar" : "Equipar"}
                </button>
                <button
                  onClick={() => onUpgrade(item.id, upgradeCost)}
                  disabled={gold < upgradeCost}
                  className="flex-1 px-3 py-2 rounded-xl text-xs font-semibold disabled:opacity-40"
                  style={{ background: "#fef3c7", color: "#d97706" }}
                >
                  Mejorar · {upgradeCost}
                </button>
              </>
            )}
            <button
              onClick={() => setConfirmSell(true)}
              className="px-3 py-2 rounded-xl text-xs font-semibold bg-rose-50 text-rose-600"
            >
              Vender
            </button>
          </div>
          {!isPotion && (
            <div className="text-xs text-slate-400 mt-2">
              <p>
                Nivel {item.level} → {item.level + 1}
              </p>
              {item.stats.map((stat) => (
                <p key={stat}>
                  {stat} →{" "}
                  {stat.replace(/(\d+)/, (value) => String(Number(value) + 1))}
                </p>
              ))}
            </div>
          )}
        </>
      )}

      {confirmSell && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4"
          style={{ background: "rgba(30,27,75,0.65)" }}
          onClick={() => setConfirmSell(false)}
        >
          <div
            className="bg-white rounded-3xl p-6 w-full max-w-sm shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="w-14 h-14 rounded-2xl bg-rose-50 flex items-center justify-center text-3xl mb-4">
              💰
            </div>
            <h2 className="text-xl font-bold text-slate-800">
              ¿Vender {item.name}?
            </h2>
            <p className="text-sm text-slate-500 mt-2">
              {item.quantity > 1
                ? "Se venderá una unidad de este objeto."
                : "El objeto desaparecerá de tu inventario."}{" "}
              Recibirás exactamente el 70% de su valor de compra.
            </p>
            <div className="rounded-2xl bg-amber-50 text-amber-700 font-bold p-3 mt-4 text-center">
              Vender por {salePrice} oro
            </div>
            <div className="flex gap-2 mt-5">
              <button
                onClick={() => setConfirmSell(false)}
                className="flex-1 py-2.5 rounded-xl border border-slate-200 text-sm font-semibold text-slate-600"
              >
                Cancelar
              </button>
              <button
                onClick={() => {
                  onSell(item.id)
                  setConfirmSell(false)
                }}
                className="flex-1 py-2.5 rounded-xl bg-rose-600 text-white text-sm font-semibold"
              >
                Confirmar venta
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
export default function Character({
  stats,
  playerName = "Aventurero",
  avatar = "🧙",
  inventory,
  initialTab = "character",
}) {
  const [loadedStats, setLoadedStats] = useState(stats ?? null)
  const [statsError, setStatsError] = useState("")
  const [items, setItems] = useState(inventory ?? INITIAL_INVENTORY)

  useEffect(() => {
    let cancelled = false

    getCharacterStats()
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
        if (!cancelled) {
          setStatsError(error.message)
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  stats = loadedStats ?? INITIAL_STATS
  const displayName = loadedStats?.playerName ?? playerName

  const [tab, setTab] = useState(initialTab)
  const [filter, setFilter] = useState("Todos")
  const [feedback, setFeedback] = useState(null)
  const xpPct = Math.round((stats.xp / stats.xpToNext) * 100)
  const equipment = items.filter((i) => i.category !== "Poción")
  const potions = items.filter((i) => i.category === "Poción")
  const filtered = equipment.filter(
    (item) => filter === "Todos" || item.category === filter,
  )
  function changeTab(next) {
    setTab(next)
  }
  function showFeedback(message) {
    setFeedback(message)
    window.setTimeout(() => setFeedback(null), 2200)
  }
  function buy(item) {
    if (stats.gold < item.buyPrice) {
      showFeedback("No tienes suficiente oro.")
      return false
    }
    setLoadedStats((currentStats) => ({
      ...(currentStats ?? INITIAL_STATS),
      gold: stats.gold - item.buyPrice,
    }))
    setItems((currentItems) => {
      const existingPotion = item.category === "Poción"
        ? currentItems.find((ownedItem) => ownedItem.catalogId === item.catalogId)
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
    showFeedback(
      `¡${item.name} añadido al inventario!`,
    )
    return true
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
      gold: stats.gold + salePrice,
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
    if (stats.gold < cost) {
      showFeedback("No tienes suficiente oro.")
      return false
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
      gold: stats.gold - cost,
    }))
    showFeedback("¡Objeto mejorado! Sus estadísticas han aumentado.")
    return true
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
          💰 {stats.gold} oro
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
            onClick={() => changeTab(key)}
            className="px-4 py-2 rounded-xl text-sm font-semibold whitespace-nowrap transition"
            style={{
              background: tab === key ? "var(--color-primary)" : "transparent",
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
        <div role="alert" className="mb-4 rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm text-rose-700">
          {statsError}
        </div>
      )}

      {tab === "character" && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 animate-fade-up">
          <div className="bg-white rounded-3xl border border-slate-100 p-6 flex flex-col items-center">
            <div
              className="w-40 h-40 rounded-3xl overflow-hidden flex items-center justify-center mb-4"
              style={{
                background: "linear-gradient(135deg, #1e1b4b, #312e81)",
              }}
            >
              {avatar?.startsWith("data:") ? (
                <AvatarDisplay
                  avatar={avatar}
                  size={160}
                  className="rounded-3xl"
                />
              ) : (
                <img
                  src={warriorImg}
                  alt="Guerrero"
                  className="w-full h-full object-contain drop-shadow-2xl"
                />
              )}
            </div>
            <h2 className="text-xl font-bold text-slate-800">{displayName}</h2>
            <p className="text-sm text-slate-500 mb-4">
              Caballero · Nivel {stats.level}
            </p>
            <div className="w-full">
              <div className="flex justify-between text-xs text-slate-500 mb-1.5">
                <span>XP actual</span>
                <span>
                  {stats.xp} / {stats.xpToNext}
                </span>
              </div>
              <div className="h-2.5 bg-slate-100 rounded-full overflow-hidden">
                <div
                  className="h-full rounded-full xp-bar-fill"
                  style={{
                    width: `${xpPct}%`,
                    background: "linear-gradient(90deg, #6366f1, #8b5cf6)",
                  }}
                />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-2 w-full mt-4">
              <div className="rounded-xl p-3 bg-amber-50 text-center">
                <p className="font-bold text-amber-600">💰 {stats.gold}</p>
                <p className="text-xs text-amber-500">Oro</p>
              </div>
              <div className="rounded-xl p-3 bg-indigo-50 text-center">
                <p className="font-bold text-indigo-600">Nv. {stats.level}</p>
                <p className="text-xs text-indigo-400">Nivel</p>
              </div>
            </div>
          </div>
          <div className="lg:col-span-2 grid sm:grid-cols-2 gap-3">
            {STAT_META.map(({ key, label, icon, color }) => {
              const val = stats[key]
              return (
                <div
                  key={key}
                  className="bg-white rounded-2xl p-4 border border-slate-100 flex items-center gap-4"
                >
                  <div
                    className="w-11 h-11 rounded-xl flex items-center justify-center text-xl"
                    style={{ background: color + "18" }}
                  >
                    {icon}
                  </div>
                  <div className="flex-1">
                    <div className="flex justify-between mb-1.5">
                      <p className="text-sm font-semibold text-slate-700">
                        {label}
                      </p>
                      <p className="text-sm font-bold" style={{ color }}>
                        {val}
                      </p>
                    </div>
                    <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                      <div
                        className="h-full rounded-full"
                        style={{
                          width: `${Math.min(val, 100)}%`,
                          background: color,
                        }}
                      />
                    </div>
                  </div>
                </div>
              )
            })}      
          </div>
        </div>
      )}

      {tab === "inventory" && (
        <div className="space-y-6 animate-fade-up">
          <section>
            <div className="flex items-end justify-between mb-3">
              <div>
                <h2 className="font-bold text-lg text-slate-800">
                  Equipamiento equipado
                </h2>
                <p className="text-xs text-slate-500">
                  Bonificaciones activas de tu personaje
                </p>
              </div>
              <span className="text-xs font-semibold text-indigo-600">
                {equipment.filter((i) => i.equipped).length} activos
              </span>
            </div>
            <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-3">
              {equipment
                .filter((i) => i.equipped)
                .map((item) => (
                  <div
                    key={item.id}
                    className="bg-indigo-950 rounded-2xl p-4 text-white flex items-center gap-3"
                  >
                    <div className="w-11 h-11 rounded-xl bg-white/10 flex items-center justify-center text-2xl">
                      {item.icon}
                    </div>
                    <div className="min-w-0">
                      <p className="font-semibold text-sm truncate">
                        {item.name}
                      </p>
                      <p className="text-xs text-indigo-200">
                        {item.category} · Nv. {item.level}
                      </p>
                    </div>
                  </div>
                ))}
            </div>
          </section>
          <section>
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-3">
              <div>
                <h2 className="font-bold text-lg text-slate-800">Inventario</h2>
                <p className="text-xs text-slate-500">
                  Equipa, mejora o vende tus objetos
                </p>
              </div>
              <div className="flex gap-1.5 overflow-x-auto pb-1">
                {CATEGORY_FILTERS.filter((c) => c !== "Poción").map(
                  (category) => (
                    <button
                      key={category}
                      onClick={() => setFilter(category)}
                      className="px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap"
                      style={{
                        background: filter === category ? "#e0e7ff" : "white",
                        color: filter === category ? "#4f46e5" : "#64748b",
                      }}
                    >
                      {category}
                    </button>
                  ),
                )}
              </div>
            </div>
            <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-3">
              {filtered.map((item) => (
                <ItemCard
                  key={item.id}
                  item={item}
                  mode="inventory"
                  gold={stats.gold}
                  onBuy={buy}
                  onSell={sell}
                  onToggleEquip={toggleEquip}
                  onUpgrade={upgrade}
                />
              ))}
            </div>
          </section>
          <section>
            <div className="mb-3">
              <h2 className="font-bold text-lg text-slate-800">
                Objetos curativos
              </h2>
              <p className="text-xs text-slate-500">
                Consumibles disponibles para los combates PvE
              </p>
            </div>
            <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-3">
              {potions.map((item) => (
                <ItemCard
                  key={item.id}
                  item={item}
                  mode="inventory"
                  gold={stats.gold}
                  onBuy={buy}
                  onSell={sell}
                  onToggleEquip={toggleEquip}
                  onUpgrade={upgrade}
                />
              ))}
            </div>
          </section>
        </div>
      )}

      {tab === "shop" && (
        <div className="animate-fade-up">
          <div className="bg-indigo-950 rounded-3xl p-5 mb-5 text-white flex flex-col sm:flex-row justify-between gap-4">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wider text-indigo-300">
                Mercado de LifeQuest
              </p>
              <h2 className="text-xl font-bold mt-1">
                Prepárate para tu próxima aventura
              </h2>
              <p className="text-sm text-indigo-200 mt-1">
                El equipamiento comprado aparecerá automáticamente en tu
                inventario.
              </p>
            </div>
            <div className="bg-white/10 rounded-2xl px-4 py-3 self-start">
              <p className="text-xs text-indigo-200">Tu saldo</p>
              <p className="text-xl font-bold text-amber-300">
                💰 {stats.gold}
              </p>
            </div>
          </div>
          <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-3">
            {SHOP_ITEMS.map((item) => (
              <ItemCard
                key={item.id}
                item={item}
                mode="shop"
                gold={stats.gold}
                onBuy={buy}
                onSell={sell}
                onToggleEquip={toggleEquip}
                onUpgrade={upgrade}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
