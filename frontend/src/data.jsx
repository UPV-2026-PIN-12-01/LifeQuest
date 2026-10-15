export const CATEGORY_META = {
  Estudio:   { color: '#6366f1', bg: '#ede9fe', icon: '📚', stats: ['Conocimiento +2', 'IQ +1'] },
  Trabajo:   { color: '#0ea5e9', bg: '#e0f2fe', icon: '💼', stats: ['Conocimiento +1', 'Estamina +1'] },
  Social:    { color: '#f472b6', bg: '#fce7f3', icon: '👥', stats: ['Vida +2', 'IQ +1'] },
  Ocio:      { color: '#a78bfa', bg: '#ede9fe', icon: '🎮', stats: ['Vida +1', 'Estamina +1'] },
  Deporte:   { color: '#10b981', bg: '#d1fae5', icon: '⚽', stats: ['Fuerza +2', 'Estamina +1'] },
  Hobbie:    { color: '#f59e0b', bg: '#fef3c7', icon: '🎨', stats: ['IQ +1', 'Conocimiento +1'] },
  Ejercicio: { color: '#ef4444', bg: '#fee2e2', icon: '💪', stats: ['Fuerza +2', 'Velocidad +1'] },
};

export const DAYS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

export const INITIAL_TASKS = [
  { id: '1', name: 'Estudiar Álgebra',  date: '2026-09-21', startTime: '09:00', endTime: '10:30', category: 'Estudio',   repeat: 'Nunca',   priority: 'alta',   day: 'Lun' },
  { id: '2', name: 'Reunión de equipo', date: '2026-09-21', startTime: '11:00', endTime: '12:00', category: 'Trabajo',   repeat: 'Nunca',   priority: 'media',  day: 'Lun', userStatus: 'completed' },
  { id: '3', name: 'Correr 5km',        date: '2026-09-22', startTime: '07:00', endTime: '08:00', category: 'Ejercicio', repeat: 'Diario',  priority: 'media',  day: 'Mar', userStatus: 'completed' },
  { id: '4', name: 'Leer un libro',     date: '2026-09-22', startTime: '20:00', endTime: '21:00', category: 'Hobbie',   repeat: 'Nunca',   priority: 'baja',   day: 'Mar' },
  { id: '5', name: 'Clases de Piano',   date: '2026-09-23', startTime: '17:00', endTime: '18:30', category: 'Hobbie',   repeat: 'Semanal', priority: 'baja',   day: 'Mié' },
  { id: '6', name: 'Cena con amigos',   date: '2026-09-23', startTime: '20:00', endTime: '22:00', category: 'Social',   repeat: 'Nunca',   priority: 'media',  day: 'Mié' },
  { id: '7', name: 'Proyecto Final',    date: '2026-09-24', startTime: '10:00', endTime: '13:00', category: 'Trabajo',  repeat: 'Nunca',   priority: 'máxima', day: 'Jue' },
  { id: '8', name: 'Fútbol sala',       date: '2026-09-25', startTime: '19:00', endTime: '20:30', category: 'Deporte',  repeat: 'Semanal', priority: 'media',  day: 'Vie' },
  { id: '9', name: 'Partida con amigos',date: '2026-09-27', startTime: '16:00', endTime: '18:00', category: 'Ocio',     repeat: 'Nunca',   priority: 'baja',   day: 'Sáb' },
];

export const INITIAL_GOALS = [
  { id: '1', title: 'Terminar el TFG', description: 'Completar y entregar el trabajo fin de grado antes del 30 de septiembre.', startDate: '2026-09-01', endDate: '2026-09-30', status: 'pending', xpReward: 500, goldReward: 200 },
  { id: '2', title: 'Correr 3 veces por semana', description: 'Mantener una rutina de running durante todo el mes.', startDate: '2026-09-01', endDate: '2026-09-30', status: 'pending', xpReward: 300, goldReward: 100 },
  { id: '3', title: 'Leer 2 libros', description: 'Leer dos libros de desarrollo personal este mes.', startDate: '2026-09-01', endDate: '2026-09-30', status: 'completed', xpReward: 200, goldReward: 80 },
];

export const INITIAL_STATS = {
  level: 7,
  xp: 2340,
  xpToNext: 3000,
  gold: 485,
  streak: 12,
  hp: 78,
  strength: 45,
  knowledge: 82,
  speed: 61,
  stamina: 55,
};

export const EQUIPMENT = [
  { id: '1', slot: 'Espada', name: 'Espada de Hierro', level: 1, bonus: '+2 Fuerza', icon: '⚔️', cost: 100 },
  { id: '2', slot: 'Armadura', name: 'Armadura de Cuero', level: 1, bonus: '+3 Vida', icon: '🛡️', cost: 120 },
  { id: '3', slot: 'Casco', name: 'Yelmo de Bronce', level: 2, bonus: '+1 Defensa', icon: '⛑️', cost: 90 },
  { id: '4', slot: 'Accesorio', name: 'Amuleto de Sabiduría', level: 1, bonus: '+2 Conocimiento', icon: '📿', cost: 80 },
];

export const RARITY_META = {
  Common: { color: '#64748b', bg: '#f1f5f9', border: '#cbd5e1' },
  Rare: { color: '#0284c7', bg: '#e0f2fe', border: '#7dd3fc' },
  Epic: { color: '#7c3aed', bg: '#ede9fe', border: '#c4b5fd' },
  Mythic: { color: '#db2777', bg: '#fce7f3', border: '#f9a8d4' },
  Legendary: { color: '#d97706', bg: '#fef3c7', border: '#fcd34d' },
  Celestial: { color: '#4f46e5', bg: '#e0e7ff', border: '#a5b4fc' },
};

export const SHOP_ITEMS = [
  { id: 'shop-sword', catalogId: 'sword', name: 'Espada del Alba', icon: '⚔️', category: 'Arma', rarity: 'Rare', level: 3, stats: ['Fuerza +8', 'Velocidad +2'], buyPrice: 260 },
  { id: 'shop-bow', catalogId: 'bow', name: 'Arco del Rastreador', icon: '🏹', category: 'Arma', rarity: 'Epic', level: 5, stats: ['Fuerza +11', 'Velocidad +6'], buyPrice: 480 },
  { id: 'shop-armor', catalogId: 'armor', name: 'Coraza del Guardián', icon: '🛡️', category: 'Armadura', rarity: 'Rare', level: 4, stats: ['Vida +18', 'Estamina +5'], buyPrice: 340 },
  { id: 'shop-helm', catalogId: 'helm', name: 'Yelmo de Claridad', icon: '⛑️', category: 'Casco', rarity: 'Epic', level: 5, stats: ['Inteligencia +12', 'Vida +6'], buyPrice: 410 },
  { id: 'shop-boots', catalogId: 'boots', name: 'Botas del Viento', icon: '🥾', category: 'Botas', rarity: 'Mythic', level: 7, stats: ['Velocidad +16', 'Estamina +8'], buyPrice: 690 },
  { id: 'shop-amulet', catalogId: 'amulet', name: 'Amuleto Celestial', icon: '🔮', category: 'Accesorio', rarity: 'Celestial', level: 10, stats: ['Inteligencia +24', 'Vida +15'], buyPrice: 1250 },
  { id: 'shop-health', catalogId: 'health-potion', name: 'Poción de vida', icon: '🧪', category: 'Poción', rarity: 'Common', level: 1, stats: ['Recupera 35 de vida'], heal: 35, quantity: 1, buyPrice: 55 },
  { id: 'shop-greater-health', catalogId: 'greater-health-potion', name: 'Poción de vida mayor', icon: '⚗️', category: 'Poción', rarity: 'Rare', level: 4, stats: ['Recupera 70 de vida'], heal: 70, quantity: 1, buyPrice: 120 },
  { id: 'shop-stamina', catalogId: 'stamina-potion', name: 'Poción de estamina', icon: '🍵', category: 'Poción', rarity: 'Common', level: 1, stats: ['Recupera 45 de estamina'], staminaRestore: 45, quantity: 1, buyPrice: 45 },
];

export const INITIAL_INVENTORY = [
  { id: 'inv-1', catalogId: 'iron-sword', name: 'Espada de Hierro', icon: '⚔️', category: 'Arma', rarity: 'Common', level: 2, stats: ['Fuerza +5'], buyPrice: 140, equipped: true },
  { id: 'inv-2', catalogId: 'leather-armor', name: 'Armadura de Cuero', icon: '🛡️', category: 'Armadura', rarity: 'Rare', level: 3, stats: ['Vida +12', 'Estamina +3'], buyPrice: 220, equipped: true },
  { id: 'inv-3', catalogId: 'bronze-helm', name: 'Yelmo de Bronce', icon: '⛑️', category: 'Casco', rarity: 'Common', level: 2, stats: ['Vida +6'], buyPrice: 110 },
  { id: 'inv-4', catalogId: 'traveler-boots', name: 'Botas de Viajero', icon: '🥾', category: 'Botas', rarity: 'Rare', level: 3, stats: ['Velocidad +5'], buyPrice: 185, equipped: true },
  { id: 'inv-5', catalogId: 'wisdom-ring', name: 'Anillo de Sabiduría', icon: '💍', category: 'Accesorio', rarity: 'Epic', level: 4, stats: ['Inteligencia +9'], buyPrice: 320 },
  { id: 'inv-6', catalogId: 'health-potion', name: 'Poción de vida', icon: '🧪', category: 'Poción', rarity: 'Common', level: 1, stats: ['Recupera 35 de vida'], heal: 35, quantity: 3, buyPrice: 55 },
  { id: 'inv-7', catalogId: 'stamina-potion', name: 'Poción de estamina', icon: '🍵', category: 'Poción', rarity: 'Common', level: 1, stats: ['Recupera 45 de estamina'], staminaRestore: 45, quantity: 2, buyPrice: 45 },
];

export const ENEMIES = [
  { id: 'goblin', name: 'Goblin salvaje', kind: 'monster', icon: '👺', level: 5, hp: 76, strength: 36, speed: 48, intelligence: 22, stamina: 70, xpReward: 110, goldReward: 42 },
  { id: 'wolf', name: 'Lobo sombrío', kind: 'monster', icon: '🐺', level: 7, hp: 92, strength: 44, speed: 72, intelligence: 28, stamina: 82, xpReward: 145, goldReward: 55 },
  { id: 'skeleton', name: 'Guardián esqueleto', kind: 'monster', icon: '💀', level: 9, hp: 115, strength: 52, speed: 43, intelligence: 46, stamina: 90, xpReward: 180, goldReward: 68 },
  { id: 'troll', name: 'Troll del pantano', kind: 'monster', icon: '🧌', level: 11, hp: 148, strength: 67, speed: 31, intelligence: 20, stamina: 105, xpReward: 225, goldReward: 86 },
  { id: 'mountain-lord', name: 'Señor de la Montaña', kind: 'boss', icon: '🗿', level: 15, hp: 240, strength: 82, speed: 48, intelligence: 64, stamina: 145, xpReward: 520, goldReward: 210 },
  { id: 'dragon', name: 'Dragón Carmesí', kind: 'boss', icon: '🐉', level: 22, hp: 360, strength: 108, speed: 76, intelligence: 95, stamina: 190, xpReward: 850, goldReward: 390 },
];

export const RANKING_PLAYERS = [
  { id: 1, name: 'LunaArcana', avatar: '🧝‍♀️', level: 28, score: 18420, online: true },
  { id: 2, name: 'KaelStorm', avatar: '🧙‍♂️', level: 27, score: 17980, online: true },
  { id: 3, name: 'NyxShadow', avatar: '🥷', level: 26, score: 16840, online: false },
  { id: 4, name: 'AresPrime', avatar: '🦸', level: 24, score: 15320, online: true },
  { id: 5, name: 'MiraSol', avatar: '🧚', level: 22, score: 14760, online: false },
  { id: 6, name: 'RookFrost', avatar: '🧔', level: 21, score: 13910, online: true },
  { id: 7, name: 'Elara', avatar: '🧝', level: 20, score: 13120, online: true },
  { id: 8, name: 'Thorn', avatar: '🦹', level: 19, score: 12470, online: false },
  { id: 9, name: 'NovaQuest', avatar: '🧑‍🚀', level: 18, score: 11830, online: true },
  { id: 10, name: 'Fenrir', avatar: '🐺', level: 17, score: 10920, online: false },
  { id: 11, name: 'IrisRune', avatar: '🧙‍♀️', level: 16, score: 10140, online: true },
  { id: 12, name: 'DanteX', avatar: '🧛', level: 15, score: 9620, online: false },
  { id: 13, name: 'CoraBloom', avatar: '🌺', level: 14, score: 8910, online: true },
  { id: 14, name: 'Atlas', avatar: '🗿', level: 13, score: 8250, online: false },
  { id: 15, name: 'SkyForge', avatar: '🦅', level: 12, score: 7740, online: true },
];

export const CATEGORY_STAT_GAINS = {
  Estudio:   { knowledge: 2 },
  Trabajo:   { knowledge: 1, stamina: 1 },
  Social:    { hp: 2 },
  Ocio:      { hp: 1, stamina: 1 },
  Deporte:   { strength: 2, stamina: 1 },
  Hobbie:    { knowledge: 1 },
  Ejercicio: { strength: 2, speed: 1 },
};

export const DIFFICULTY_META = {
  'fácil':       { label: 'Fácil',       color: '#16a34a', bg: '#dcfce7', dot: '🟢', xp: 25,  gold: 10 },
  'normal':      { label: 'Normal',      color: '#ca8a04', bg: '#fef9c3', dot: '🟡', xp: 40,  gold: 15 },
  'difícil':     { label: 'Difícil',     color: '#ea580c', bg: '#ffedd5', dot: '🟠', xp: 75,  gold: 25 },
  'muy difícil': { label: 'Muy difícil', color: '#dc2626', bg: '#fee2e2', dot: '🔴', xp: 120, gold: 40 },
};

export const CONDITION_PRESETS = [
  { label: 'Tiempo riguroso',      difficulty: 'fácil',       desc: 'Completa la tarea respetando estrictamente el tiempo establecido.' },
  { label: 'No pedir ayuda',        difficulty: 'normal',      desc: 'Resuelve la actividad sin solicitar ayuda a nadie.' },
  { label: 'No mirar el móvil',     difficulty: 'difícil',     desc: 'No consultar el móvil durante toda la sesión.' },
  { label: 'Sin interrupciones',    difficulty: 'difícil',     desc: 'Completa la actividad sin ninguna interrupción externa.' },
  { label: 'No usar solucionario',  difficulty: 'difícil',     desc: 'Resolver los ejercicios sin consultar las soluciones.' },
  { label: 'Utilizar método nuevo', difficulty: 'muy difícil', desc: 'Resolver utilizando un método que no hayas utilizado anteriormente.' },
  { label: 'Condición personalizada', difficulty: 'normal',    desc: '', custom: true },
];

export const INITIAL_CHALLENGES = [
  {
    id: 'c1', taskId: '1', taskName: 'Estudiar Álgebra', taskCategory: 'Estudio',
    condition: 'No usar solucionario', conditionDesc: 'Resolver los ejercicios sin consultar las soluciones.',
    difficulty: 'difícil', status: 'available', xpReward: 75, goldReward: 25, createdDate: '2026-09-21',
  },
  {
    id: 'c2', taskId: '3', taskName: 'Correr 5km', taskCategory: 'Ejercicio',
    condition: 'Tiempo riguroso', conditionDesc: 'Completar el entrenamiento dentro del tiempo planificado.',
    difficulty: 'normal', status: 'active', xpReward: 40, goldReward: 15, createdDate: '2026-09-22',
  },
  {
    id: 'c3', taskId: '7', taskName: 'Proyecto Final', taskCategory: 'Trabajo',
    condition: 'Sin interrupciones', conditionDesc: 'Completar la sesión de trabajo sin ninguna interrupción externa.',
    difficulty: 'muy difícil', status: 'available', xpReward: 120, goldReward: 40, createdDate: '2026-09-24',
  },
  {
    id: 'c4', taskId: '2', taskName: 'Reunión de equipo', taskCategory: 'Trabajo',
    condition: 'No mirar el móvil', conditionDesc: 'No consultar el móvil durante toda la sesión.',
    difficulty: 'difícil', status: 'completed', xpReward: 75, goldReward: 25, createdDate: '2026-09-21', selfRating: 8,
  },
];

export const ACHIEVEMENTS = [
  { id: '1', icon: '👣', title: 'Primer Paso', desc: 'Completa tu primera tarea.', unlocked: true },
  { id: '2', icon: '⚡', title: 'Productivo', desc: 'Completa 5 tareas.', unlocked: true },
  { id: '3', icon: '🔥', title: 'Constante', desc: 'Consigue una racha de 7 días.', unlocked: true },
  { id: '4', icon: '🎯', title: 'Enfocado', desc: 'Completa 5 desafíos.', unlocked: false },
  { id: '5', icon: '⭐', title: 'Nivel 10', desc: 'Alcanza el nivel 10.', unlocked: false },
  { id: '6', icon: '💎', title: 'Maestro del Tiempo', desc: 'Registra 100 horas de estudio.', unlocked: false },
  { id: '7', icon: '🏆', title: 'Campeón', desc: 'Completa 50 tareas.', unlocked: false },
  { id: '8', icon: '🌟', title: 'Imparable', desc: 'Mantén una racha de 30 días.', unlocked: false },
];