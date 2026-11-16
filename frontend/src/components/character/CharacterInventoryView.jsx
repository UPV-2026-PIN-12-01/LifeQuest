import { useState } from "react"
import { RARITY_META } from "../../data"

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
      className="character-rarity-badge text-xs px-2 py-0.5 rounded-full font-semibold"
      style={{
        "--character-rarity-color": meta.color,
        "--character-rarity-bg": meta.bg,
      }}
    >
      {rarity}
    </span>
  )
}

// Why the shop cannot sell this item now; null when it can.
function getBuyBlocker(item, gold, level) {
  if (level < item.level) return "Nivel insuficiente"
  if (gold < item.buyPrice) return "Oro insuficiente"
  return null
}

function ItemCard({
  item,
  mode,
  gold,
  level,
  onBuy,
  onSell,
  onToggleEquip,
  onUpgrade,
  busy,
}) {
  const [confirmSell, setConfirmSell] = useState(false)
  const upgradeCost = item.level * item.baseUpgradePrice
  const salePrice = Math.floor(item.buyPrice * 0.7)
  const isPotion = item.category === "Poción"
  const buyBlocker = mode === "shop" ? getBuyBlocker(item, gold, level) : null

  return (
    <div
      className="character-item-card bg-white rounded-2xl border p-4 flex flex-col"
      style={{
        "--character-item-border-color": item.equipped
          ? RARITY_META[item.rarity].border
          : "#e2e8f0",
      }}
    >
      <div className="flex items-start gap-3">
        <div
          className="character-item-icon w-12 h-12 rounded-2xl flex items-center justify-center text-2xl shrink-0"
          style={{ "--character-item-rarity-bg": RARITY_META[item.rarity].bg }}
        >
          {item.icon}
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <p className="font-semibold text-sm text-slate-800 leading-tight">
              {item.name}
            </p>
            {item.quantity > 1 && (
              <span className="text-xs font-bold text-indigo-600">
                ×{item.quantity}
              </span>
            )}
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
            disabled={busy || buyBlocker !== null}
            title={
              buyBlocker === "Nivel insuficiente"
                ? `Necesitas nivel ${item.level}`
                : undefined
            }
            className="character-buy-button px-4 py-2 rounded-xl text-xs font-semibold text-black disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {buyBlocker ?? "Comprar"}
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
                  disabled={busy}
                  className={`character-equip-button flex-1 px-3 py-2 rounded-xl text-xs font-semibold${item.equipped ? " is-equipped" : ""}`}
                >
                  {item.equipped ? "Desequipar" : "Equipar"}
                </button>
                <button
                  onClick={() => onUpgrade(item.id)}
                  disabled={
                    busy || gold < upgradeCost || item.level >= item.maxLevel
                  }
                  className="character-upgrade-button flex-1 px-3 py-2 rounded-xl text-xs font-semibold disabled:opacity-40"
                >
                  Mejorar · {upgradeCost}
                </button>
              </>
            )}
            <button
              onClick={() => setConfirmSell(true)}
              disabled={busy || item.equipped}
              title={
                item.equipped ? "Desequipa el objeto antes de venderlo" : undefined
              }
              className="px-3 py-2 rounded-xl text-xs font-semibold bg-rose-50 text-rose-600 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Vender
            </button>
          </div>
          {item.equipped && (
            <p className="text-xs text-slate-400 mt-2">
              Desequipa el objeto antes de venderlo.
            </p>
          )}
          {!isPotion && item.level < item.maxLevel && (
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
          className="character-sell-backdrop fixed inset-0 z-50 flex items-center justify-center p-4"
          onClick={() => setConfirmSell(false)}
        >
          <div
            className="bg-white rounded-3xl p-6 w-full max-w-sm shadow-2xl"
            onClick={(event) => event.stopPropagation()}
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
                disabled={busy}
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

export default function CharacterInventoryView({
  tab,
  market,
  marketLoading,
  gold,
  level,
  filter,
  setFilter,
  onBuy,
  onSell,
  onToggleEquip,
  onUpgrade,
  busy,
}) {
  const items = market?.inventory ?? []
  const equipment = items.filter((item) => item.category !== "Poción")
  const potions = items.filter((item) => item.category === "Poción")
  const [shopFilter, setShopFilter] = useState("Todos")
  const shopItems = (market?.shopItems ?? []).filter(
    (item) => shopFilter === "Todos" || item.category === shopFilter,
  )
  const filtered = equipment.filter(
    (item) => filter === "Todos" || item.category === filter,
  )

  if (tab === "inventory") {
    return (
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
              {equipment.filter((item) => item.equipped).length} activos
            </span>
          </div>
          <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-3">
            {equipment
              .filter((item) => item.equipped)
              .map((item) => (
                <div
                  key={item.id}
                  className="bg-indigo-950 rounded-2xl p-4 text-white flex items-center gap-3"
                >
                  <div className="w-11 h-11 rounded-xl bg-white/10 flex items-center justify-center text-2xl">
                    {item.icon}
                  </div>
                  <div className="min-w-0">
                    <p className="font-semibold text-sm truncate">{item.name}</p>
                    <p className="text-xs text-indigo-200">
                      {item.category} · Nv. {item.level}
                    </p>
                  </div>
                </div>
              ))}
            {market && equipment.every((item) => !item.equipped) && (
              <p className="text-sm text-slate-500 sm:col-span-2 lg:col-span-4">
                Aún no tienes objetos equipados. Compra equipo en la tienda y
                equípalo desde tu inventario.
              </p>
            )}
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
              {CATEGORY_FILTERS.filter((category) => category !== "Poción").map(
                (category) => (
                  <button
                    key={category}
                    onClick={() => setFilter(category)}
                    className={`character-filter-button px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap${filter === category ? " is-active" : ""}`}
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
                gold={gold}
                onBuy={onBuy}
                onSell={onSell}
                onToggleEquip={onToggleEquip}
                onUpgrade={onUpgrade}
                busy={busy}
              />
            ))}
            {market && filtered.length === 0 && (
              <p className="text-sm text-slate-500 sm:col-span-2 xl:col-span-3">
                Aún no tienes objetos en esta categoría.
              </p>
            )}
          </div>
        </section>
        <section>
          <div className="mb-3">
            <h2 className="font-bold text-lg text-slate-800">Objetos curativos</h2>
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
                gold={gold}
                onBuy={onBuy}
                onSell={onSell}
                onToggleEquip={onToggleEquip}
                onUpgrade={onUpgrade}
                busy={busy}
              />
            ))}
            {market && potions.length === 0 && (
              <p className="text-sm text-slate-500 sm:col-span-2 xl:col-span-3">
                Todavía no has comprado pociones.
              </p>
            )}
          </div>
        </section>
      </div>
    )
  }

  return (
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
            El equipamiento comprado aparecerá automáticamente en tu inventario.
          </p>
        </div>
        <div className="bg-white/10 rounded-2xl px-4 py-3 self-start">
          <p className="text-xs text-indigo-200">Tu saldo</p>
          <p className="text-xl font-bold text-amber-300">💰 {gold}</p>
        </div>
      </div>
      <div className="flex gap-1.5 overflow-x-auto pb-1 mb-3">
        {CATEGORY_FILTERS.map((category) => (
          <button
            key={category}
            onClick={() => setShopFilter(category)}
            className={`character-filter-button px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap${shopFilter === category ? " is-active" : ""}`}
          >
            {category}
          </button>
        ))}
      </div>
      <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-3">
        {shopItems.map((item) => (
          <ItemCard
            key={item.id}
            item={item}
            mode="shop"
            gold={gold}
            level={level}
            onBuy={onBuy}
            onSell={onSell}
            onToggleEquip={onToggleEquip}
            onUpgrade={onUpgrade}
            busy={busy}
          />
        ))}
        {marketLoading && (
          <p className="text-sm text-slate-500 sm:col-span-2 xl:col-span-3">
            Cargando mercado...
          </p>
        )}
      </div>
    </div>
  )
}
