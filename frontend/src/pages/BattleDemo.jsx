import { useEffect, useMemo, useRef, useState } from "react"
import { Link, Navigate, useNavigate, useOutletContext } from 'react-router-dom'
import { ENEMIES } from "../data"
import warriorImg from "../assets/warrior1.png"
import { requestJson } from "../services/http"
const LOG_COLORS = {
  attack: "text-slate-700",
  damage: "text-rose-600",
  critical: "text-amber-600",
  dodge: "text-sky-600",
  heal: "text-emerald-600",
  stamina: "text-teal-600",
  system: "text-indigo-600",
}
const attackDamage = (strength) => Math.ceil(strength * 0.25)
const staminaCost = (strength) => Math.ceil(strength * 0.2)
const staminaRecovery = (current, max) => Math.ceil((max - current) * 0.2)
const critChance = (intelligence) => Math.min(100, intelligence / 10)
const critMultiplier = (intelligence) =>
  1.5 + Math.max(0, Math.floor((intelligence - 1000) / 10) * 0.01)
const dodgeChance = (ownIntelligence, rivalIntelligence) =>
  Math.min(
    50,
    Math.max(1, (ownIntelligence / Math.max(1, rivalIntelligence)) * 10),
  )
function ProgressBar({ value, max, type }) {
  const pct = Math.max(0, Math.round((value / max) * 100))
  return (
    <div>
      <div className="flex justify-between text-xs mb-1">
        <span>{type === "hp" ? "Vida" : "Estamina"}</span>
        <span>
          {Math.max(0, value)} / {max}
        </span>
      </div>
      <div className="h-2.5 rounded-full bg-slate-200 overflow-hidden">
        <div
          className="h-full rounded-full transition-all duration-500"
          style={{
            width: `${pct}%`,
            background: type === "hp" ? "#ef4444" : "#10b981",
          }}
        />
      </div>
    </div>
  )
}
function CombatStats({ strength, speed, intelligence, rivalIntelligence }) {
  return (
    <div className="grid grid-cols-2 gap-2 mt-3 text-xs">
      <div className="rounded-lg bg-slate-50 p-2">
        <span className="text-slate-400">Daño</span>
        <p className="font-bold text-slate-700">{attackDamage(strength)}</p>
      </div>
      <div className="rounded-lg bg-slate-50 p-2">
        <span className="text-slate-400">Velocidad</span>
        <p className="font-bold text-slate-700">{speed}</p>
      </div>
      <div className="rounded-lg bg-slate-50 p-2">
        <span className="text-slate-400">Crítico</span>
        <p className="font-bold text-violet-600">
          {critChance(intelligence).toFixed(1)}% · ×
          {critMultiplier(intelligence).toFixed(2)}
        </p>
      </div>
      <div className="rounded-lg bg-slate-50 p-2">
        <span className="text-slate-400">Esquiva</span>
        <p className="font-bold text-sky-600">
          {dodgeChance(intelligence, rivalIntelligence).toFixed(1)}%
        </p>
      </div>
    </div>
  )
}
export default function Combat({
  stats ={hp:500, strength:200, stamina:10, speed:999, knowledge:10},
  playerName,
  inventory = [{category:"poción", quantity:5}],
  onUsePotion,
  onReward,
}) {
  const [selected, setSelected] = useState(ENEMIES[0])
  const [battle, setBattle] = useState(null)
  const [logs, setLogs] = useState([])
  const [showPotions, setShowPotions] = useState(false)
  const [monsterAvailable, setMonsterAvailable] = useState(true)
  const [bossAvailable, setBossAvailable] = useState(true)
  const logId = useRef(0)
  const rewarded = useRef(false)
  const potions = useMemo(
    () =>
      inventory.filter((i) => i.category === "Poción" && (i.quantity ?? 0) > 0),
    [inventory],
  )
  const playerIntelligence = stats.knowledge
  function addLogs(entries) {
    setLogs((current) => [
      ...current,
      ...entries.map((entry) => ({ ...entry, id: ++logId.current })),
    ])
  }
  function startBattle(enemy) {
    if (
      (enemy.kind === "monster" && !monsterAvailable) ||
      (enemy.kind === "boss" && !bossAvailable)
    )
      return
    rewarded.current = false
    const first = stats.speed >= enemy.speed ? "player" : "enemy"
    setSelected(enemy)
    setLogs([
      {
        id: ++logId.current,
        kind: "system",
        text: `Comienza el combate contra ${enemy.name}. ${
          first === "player" ? playerName : enemy.name
        } actúa primero por velocidad.`,
      },
    ])
    setBattle({
      enemy,
      player: {
        hp: stats.hp,
        maxHp: stats.hp,
        stamina: stats.stamina,
        maxStamina: stats.stamina,
      },
      rival: {
        hp: enemy.hp,
        maxHp: enemy.hp,
        stamina: enemy.stamina,
        maxStamina: enemy.stamina,
      },
      turn: first,
      status: "active",
    })
  }
  function performAttack(attacker) {
    setBattle((current) => {
      if (!current || current.status !== "active" || current.turn !== attacker)
        return current
      const isPlayer = attacker === "player"
      const attackerName = isPlayer ? playerName : current.enemy.name
      const defenderName = isPlayer ? current.enemy.name : playerName
      const strength = isPlayer ? stats.strength : current.enemy.strength
      const intelligence = isPlayer
        ? playerIntelligence
        : current.enemy.intelligence
      const defenderIntelligence = isPlayer
        ? current.enemy.intelligence
        : playerIntelligence
      const attackerState = isPlayer ? current.player : current.rival
      const defenderState = isPlayer ? current.rival : current.player
      const cost = staminaCost(strength)
      const nextStamina = Math.max(0, attackerState.stamina - cost)
      const recovery = staminaRecovery(nextStamina, attackerState.maxStamina)
      const recoveredStamina = Math.min(
        attackerState.maxStamina,
        nextStamina + recovery,
      )
      const dodged =
        Math.random() * 100 < dodgeChance(defenderIntelligence, intelligence)
      const critical = !dodged && Math.random() * 100 < critChance(intelligence)
      const damage = dodged
        ? 0
        : Math.ceil(
            attackDamage(strength) *
              (critical ? critMultiplier(intelligence) : 1),
          )
      const nextHp = Math.max(0, defenderState.hp - damage)
      const finished = nextHp === 0
      const nextStatus = finished ? (isPlayer ? "victory" : "defeat") : "active"
      const nextTurn = isPlayer ? "enemy" : "player"
      const entries = [
        {
          kind: "attack",
          text: `${attackerName} ha atacado a ${defenderName}.`,
        },
        {
          kind: "stamina",
          text: `${attackerName} consume ${cost} de estamina.`,
        },
      ]
      if (dodged)
        entries.push({
          kind: "dodge",
          text: `¡${defenderName} esquiva el ataque!`,
        })
      else {
        if (critical)
          entries.push({ kind: "critical", text: "¡Golpe crítico!" })
        entries.push({
          kind: "damage",
          text: `${defenderName} recibe ${damage} de daño.`,
        })
      }
      if (recovery > 0)
        entries.push({
          kind: "stamina",
          text: `${attackerName} recupera ${recovery} de estamina.`,
        })
      if (finished)
        entries.push({
          kind: "system",
          text: isPlayer
            ? `¡${current.enemy.name} ha sido derrotado!`
            : `${playerName} ya no puede continuar.`,
        })
      window.setTimeout(() => addLogs(entries), 0)
      return {
        ...current,
        player: isPlayer
          ? { ...current.player, stamina: recoveredStamina }
          : { ...current.player, hp: nextHp },
        rival: isPlayer
          ? { ...current.rival, hp: nextHp }
          : { ...current.rival, stamina: recoveredStamina },
        turn: nextTurn,
        status: nextStatus,
      }
    })
  }
  useEffect(() => {
    if (!battle || battle.status !== "active" || battle.turn !== "enemy") return
    const timer = window.setTimeout(() => performAttack("enemy"), 850)
    return () => window.clearTimeout(timer)
  }, [battle?.turn, battle?.status])
  useEffect(() => {
    if (!battle || battle.status === "active" || rewarded.current) return
    rewarded.current = true
    if (battle.status === "victory")
      console.log(battle.enemy.goldReward);
    if (battle.enemy.kind === "monster") setMonsterAvailable(false)
    else setBossAvailable(false)
  }, [battle?.status])
  function usePotion(event, item) {
    event.preventDefault()
    event.stopPropagation()
    if (!battle || battle.turn !== "player" || battle.status !== "active")
      return
    const hpGain = Math.min(
      item.heal ?? 0,
      battle.player.maxHp - battle.player.hp,
    )
    const staminaGain = Math.min(
      item.staminaRestore ?? 0,
      battle.player.maxStamina - battle.player.stamina,
    )
    const staminaAfterPotion = battle.player.stamina + staminaGain
    const recovery = staminaRecovery(
      staminaAfterPotion,
      battle.player.maxStamina,
    )
    setBattle({
      ...battle,
      player: {
        ...battle.player,
        hp: battle.player.hp + hpGain,
        stamina: Math.min(
          battle.player.maxStamina,
          staminaAfterPotion + recovery,
        ),
      },
      turn: "enemy",
    })
    addLogs([
      { kind: "heal", text: `${playerName} ha usado ${item.name}.` },
      {
        kind: "heal",
        text: `${playerName} recupera ${hpGain} de vida${
          staminaGain ? ` y ${staminaGain} de estamina` : ""
        }.`,
      },
      ...(recovery
        ? [
            {
              kind: "stamina",
              text: `${playerName} recupera ${recovery} de estamina al final de su turno.`,
            },
          ]
        : []),
    ])
    onUsePotion(item.id)
    setShowPotions(false)
  }
  if (battle) {
    const resultOpen = battle.status !== "active"
    return (
      <div className="p-4 lg:p-6 max-w-6xl mx-auto">
        <div className="flex items-center justify-between mb-5">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-indigo-500">
              Combate PvE ·{" "}
              {battle.enemy.kind === "boss"
                ? "Jefe semanal"
                : "Monstruo diario"}
            </p>
            <h1 className="text-2xl font-bold text-slate-800">
              Arena de combate
            </h1>
          </div>
          <button
            type="button"
            onClick={() => setBattle(null)}
            className="button"
          >
            Abandonar
          </button>
        </div>

        <div
          className="rounded-3xl p-4 sm:p-6 mb-4"
          style={{ background: "linear-gradient(145deg,#1e1b4b,#312e81)" }}
        >
          <div className="text-center mb-4">
            <span
              className={`inline-flex px-4 py-2 rounded-full text-xs font-bold ${
                battle.status === "active"
                  ? "bg-white/10 text-white"
                  : "bg-amber-400 text-amber-950"
              }`}
            >
              {battle.status === "active"
                ? battle.turn === "player"
                  ? `Tu turno, ${playerName}`
                  : `Turno de ${battle.enemy.name}...`
                : "Combate finalizado"}
            </span>
          </div>
          <div className="grid grid-cols-[1fr_auto_1fr] gap-2 sm:gap-6 items-center">
            <div
              className={`bg-white rounded-2xl p-3 sm:p-5 transition ${
                battle.turn === "player" && battle.status === "active"
                  ? "ring-4 ring-indigo-400"
                  : ""
              }`}
            >
              <div className="flex flex-col sm:flex-row items-center gap-3 mb-4">
                <div className="w-16 h-16 rounded-2xl bg-indigo-950 overflow-hidden">
                  <img
                    src={warriorImg}
                    alt={playerName}
                    className="w-full h-full object-contain"
                  />
                </div>
                <div className="text-center sm:text-left">
                  <p className="font-bold text-slate-800">{playerName}</p>
                  <p className="text-xs text-slate-400">
                    Nivel {stats.level} · Caballero
                  </p>
                </div>
              </div>
              <div className="space-y-2">
                <ProgressBar
                  value={battle.player.hp}
                  max={battle.player.maxHp}
                  type="hp"
                />
                <ProgressBar
                  value={battle.player.stamina}
                  max={battle.player.maxStamina}
                  type="stamina"
                />
              </div>
              <div className="hidden sm:block">
                <CombatStats
                  strength={stats.strength}
                  speed={stats.speed}
                  intelligence={playerIntelligence}
                  rivalIntelligence={battle.enemy.intelligence}
                />
              </div>
            </div>
            <div className="text-white font-black text-lg sm:text-2xl">VS</div>
            <div
              className={`bg-white rounded-2xl p-3 sm:p-5 transition ${
                battle.turn === "enemy" && battle.status === "active"
                  ? "ring-4 ring-rose-400"
                  : ""
              }`}
            >
              <div className="flex flex-col sm:flex-row-reverse items-center gap-3 mb-4">
                <div className="w-16 h-16 rounded-2xl bg-rose-50 flex items-center justify-center text-4xl">
                  {battle.enemy.icon}
                </div>
                <div className="text-center sm:text-right">
                  <p className="font-bold text-slate-800">
                    {battle.enemy.name}
                  </p>
                  <p className="text-xs text-slate-400">
                    Nivel {battle.enemy.level} ·{" "}
                    {battle.enemy.kind === "boss" ? "Jefe" : "Monstruo"}
                  </p>
                </div>
              </div>
              <div className="space-y-2">
                <ProgressBar
                  value={battle.rival.hp}
                  max={battle.rival.maxHp}
                  type="hp"
                />
                <ProgressBar
                  value={battle.rival.stamina}
                  max={battle.rival.maxStamina}
                  type="stamina"
                />
              </div>
              <div className="hidden sm:block">
                <CombatStats
                  strength={battle.enemy.strength}
                  speed={battle.enemy.speed}
                  intelligence={battle.enemy.intelligence}
                  rivalIntelligence={playerIntelligence}
                />
              </div>
            </div>
          </div>
        </div>

        <div className="grid lg:grid-cols-3 gap-4">
          <div className="lg:col-span-2 bg-white rounded-3xl border border-slate-100 p-4">
            <div className="flex items-center justify-between mb-3">
              <h2 className="font-bold text-slate-800">Acciones</h2>
              <span className="text-xs text-slate-400">
                Ataque: {attackDamage(stats.strength)} daño · Coste:{" "}
                {staminaCost(stats.strength)} EST
              </span>
            </div>
            <div className="grid grid-cols-2 gap-3">
              <button
                type="button"
                disabled={
                  battle.turn !== "player" || battle.status !== "active"
                }
                onClick={() => performAttack("player")}
                className="py-4 rounded-2xl text-white font-bold disabled:opacity-40"
                style={{
                  background: "linear-gradient(135deg,#ef4444,#f97316)",
                }}
              >
                ⚔️ ATACAR
              </button>
              <button
                type="button"
                disabled={
                  battle.turn !== "player" ||
                  battle.status !== "active" ||
                  potions.length === 0
                }
                onClick={() => setShowPotions(true)}
                className="py-4 rounded-2xl text-white font-bold disabled:opacity-40"
                style={{
                  background: "linear-gradient(135deg,#10b981,#059669)",
                }}
              >
                🧪 CURAR (
                {potions.reduce((sum, p) => sum + (p.quantity ?? 0), 0)})
              </button>
            </div>
            <div className="grid grid-cols-3 gap-2 mt-3 text-center text-xs">
              <div className="rounded-xl bg-violet-50 p-2 text-violet-700">
                <b>{critChance(playerIntelligence).toFixed(1)}%</b>
                <br />
                crítico
              </div>
              <div className="rounded-xl bg-sky-50 p-2 text-sky-700">
                <b>
                  {dodgeChance(
                    playerIntelligence,
                    battle.enemy.intelligence,
                  ).toFixed(1)}
                  %
                </b>
                <br />
                esquiva
              </div>
              <div className="rounded-xl bg-emerald-50 p-2 text-emerald-700">
                <b>20%</b>
                <br />
                recuperación
              </div>
            </div>
          </div>
          <div className="bg-white rounded-3xl border border-slate-100 p-4 max-h-72 flex flex-col">
            <h2 className="font-bold text-slate-800 mb-3">
              Registro de combate
            </h2>
            <div className="space-y-2 overflow-y-auto flex-1 pr-1">
              {logs.map((log) => (
                <p
                  key={log.id}
                  className={`text-xs leading-relaxed ${LOG_COLORS[log.kind]}`}
                >
                  › {log.text}
                </p>
              ))}
            </div>
          </div>
        </div>

        {showPotions && (
          <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
            style={{ background: "rgba(30,27,75,0.7)" }}
            onClick={() => setShowPotions(false)}
          >
            <div
              className="bg-white rounded-3xl p-6 w-full max-w-md"
              onClick={(e) => e.stopPropagation()}
            >
              <h2 className="text-xl font-bold text-slate-800">
                Elige un objeto curativo
              </h2>
              <p className="text-sm text-slate-500 mt-1">
                Usar una poción consumirá tu turno.
              </p>
              <div className="space-y-2 mt-4">
                {potions.map((item) => (
                  <button
                    type="button"
                    key={item.id}
                    onClick={(event) => usePotion(event, item)}
                    className="w-full flex items-center gap-3 p-3 rounded-2xl border border-slate-200 text-left hover:border-emerald-300"
                  >
                    <span className="w-11 h-11 rounded-xl bg-emerald-50 flex items-center justify-center text-2xl">
                      {item.icon}
                    </span>
                    <span className="flex-1">
                      <b className="text-sm text-slate-800">{item.name}</b>
                      <small className="block text-slate-400">
                        {item.heal
                          ? `+${item.heal} vida`
                          : `+${item.staminaRestore} estamina`}
                      </small>
                    </span>
                    <span className="font-bold text-indigo-600">
                      ×{item.quantity}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}

        {resultOpen && (
          <div
            className="fixed inset-0 z-50 flex items-center justify-center p-4"
            style={{ background: "rgba(30,27,75,0.75)" }}
          >
            <div className="bg-white rounded-3xl p-7 w-full max-w-md text-center shadow-2xl animate-level-up">
              <div className="text-6xl">
                {battle.status === "victory" ? "🏆" : "🛡️"}
              </div>
              <h2
                className={`text-3xl font-bold mt-3 ${
                  battle.status === "victory"
                    ? "text-indigo-600"
                    : "text-slate-700"
                }`}
              >
                {battle.status === "victory" ? "¡Victoria!" : "Derrota"}
              </h2>
              <p className="text-sm text-slate-500 mt-1">
                {battle.status === "victory"
                  ? `Has derrotado a ${battle.enemy.name}.`
                  : `${battle.enemy.name} ha ganado esta vez.`}
              </p>
              {battle.status === "victory" ? (
                <>
                  <div className="grid grid-cols-2 gap-3 mt-5">
                    <div className="rounded-2xl bg-violet-50 p-3">
                      <p className="font-bold text-violet-600">
                        +{battle.enemy.xpReward} XP
                      </p>
                      <p className="text-xs text-violet-400">Experiencia</p>
                    </div>
                    <div className="rounded-2xl bg-amber-50 p-3">
                      <p className="font-bold text-amber-600">
                        +{battle.enemy.goldReward}
                      </p>
                      <p className="text-xs text-amber-400">Oro</p>
                    </div>
                  </div>
                  <div className="rounded-2xl bg-slate-50 p-3 mt-3 text-left text-xs text-slate-600">
                    <p>Logro «Cazador» · 4/10 enemigos</p>
                    <div className="h-1.5 bg-slate-200 rounded-full mt-2">
                      <div className="w-2/5 h-full bg-indigo-500 rounded-full" />
                    </div>
                    <p className="mt-2">
                      Posible recompensa: Fragmento de runa
                    </p>
                  </div>
                </>
              ) : (
                <div className="rounded-2xl bg-slate-50 p-4 mt-5 text-sm text-slate-500">
                  Vida final: 0 · Sin recompensas de victoria.
                  <br />
                  Mejora tu equipo y vuelve a intentarlo.
                </div>
              )}
              <button
                type="button"
                onClick={() => setBattle(null)}
                className="w-full mt-5 py-3 rounded-xl text-white font-semibold"
                style={{ background: "var(--color-primary)" }}
              >
                Volver a la arena
              </button>
            </div>
          </div>
        )}
      </div>
    )
  }
  return (
    <div className="p-4 lg:p-6 max-w-6xl mx-auto">
      <div className="mb-5">
        <h1 className="text-2xl font-bold text-slate-800">Combate PvE</h1>
        <p className="text-sm text-slate-500">
          Pon a prueba el progreso que consigues completando tus objetivos.
        </p>
      </div>
      <div className="grid sm:grid-cols-2 gap-3 mb-6">
        <div className="bg-white rounded-2xl border border-slate-100 p-4 flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-emerald-50 flex items-center justify-center text-xl">
            ⚔️
          </div>
          <div className="flex-1">
            <p className="font-bold text-slate-800">MONSTRUO</p>
            <p
              className={`text-xs font-semibold ${
                monsterAvailable ? "text-emerald-600" : "text-slate-400"
              }`}
            >
              {monsterAvailable
                ? "✓ Disponible hoy"
                : "Próximo combate disponible: mañana"}
            </p>
          </div>
          <span className="text-xs text-slate-400">1 / día</span>
        </div>
        <div className="bg-white rounded-2xl border border-slate-100 p-4 flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-violet-50 flex items-center justify-center text-xl">
            👑
          </div>
          <div className="flex-1">
            <p className="font-bold text-slate-800">JEFE</p>
            <p
              className={`text-xs font-semibold ${
                bossAvailable ? "text-emerald-600" : "text-slate-400"
              }`}
            >
              {bossAvailable
                ? "✓ Disponible esta semana"
                : "Próximo combate disponible: 4 días"}
            </p>
          </div>
          <span className="text-xs text-slate-400">1 / semana</span>
        </div>
      </div>

      {["monster", "boss"].map((kind) => (
        <section key={kind} className="mb-6">
          <div className="flex items-end justify-between mb-3">
            <div>
              <h2 className="text-lg font-bold text-slate-800">
                {kind === "monster" ? "Monstruos" : "Jefes"}
              </h2>
              <p className="text-xs text-slate-500">
                {kind === "monster"
                  ? "La dificultad aumenta con cada victoria."
                  : "Enemigos superiores con grandes recompensas."}
              </p>
            </div>
          </div>
          <div className="grid sm:grid-cols-2 xl:grid-cols-4 gap-3">
            {ENEMIES.filter((e) => e.kind === kind).map((enemy) => {
              const available =
                kind === "monster" ? monsterAvailable : bossAvailable
              return (
                <div
                  key={enemy.id}
                  onClick={() => setSelected(enemy)}
                  className={`bg-white rounded-3xl border p-4 cursor-pointer transition ${
                    selected.id === enemy.id
                      ? "border-indigo-400 shadow-sm"
                      : "border-slate-100"
                  }`}
                >
                  <div
                    className={`h-24 rounded-2xl flex items-center justify-center text-5xl ${
                      kind === "boss" ? "bg-violet-50" : "bg-slate-50"
                    }`}
                  >
                    {enemy.icon}
                  </div>
                  <div className="flex items-start justify-between mt-3">
                    <div>
                      <p className="font-bold text-sm text-slate-800">
                        {enemy.name}
                      </p>
                      <p className="text-xs text-slate-400">
                        Nivel {enemy.level}
                      </p>
                    </div>
                    <span className="text-xs font-bold text-rose-500">
                      ♥ {enemy.hp}
                    </span>
                  </div>
                  <div className="grid grid-cols-2 text-xs text-slate-500 gap-1 mt-3">
                    <span>💪 {enemy.strength}</span>
                    <span>⚡ {enemy.speed}</span>
                    <span>🧠 {enemy.intelligence}</span>
                    <span>🌿 {enemy.stamina}</span>
                  </div>
                  <button
                    type="button"
                    disabled={!available}
                    onClick={(e) => {
                      e.preventDefault()
                      e.stopPropagation()
                      startBattle(enemy)
                    }}
                    className="w-full mt-4 py-2.5 rounded-xl text-sm font-semibold text-white disabled:opacity-40"
                    style={{
                      background:
                        kind === "boss" ? "#7c3aed" : "var(--color-primary)",
                    }}
                  >
                    {available ? "Combatir" : "No disponible"}
                  </button>
                </div>
              )
            })}
          </div>
        </section>
      ))}
      <div className="bg-white">
        <h3 className="font-bold">Reglas rápidas</h3>
        <div className="text-slate-500">
          <br></br>
          <p>⚔️ Daño = Fuerza × 0,5 - Estamina rival x 0,25, redondeado hacia arriba.</p>
          <p>🌿 Cada ataque consume Fuerza × 0,20 de estamina.</p>
          <p>🧠 Conocimiento aumenta crítico 10%-100% y daño crítico x2-x4.</p>
          <p>👟 Velocidad aumenta la agilidad</p>
        </div>
      </div>
    </div>
  )
}
