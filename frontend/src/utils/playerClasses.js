import arquero from '../assets/classes/arquero.svg'
import guerrero from '../assets/classes/guerrero.svg'
import mago from '../assets/classes/mago.svg'

export const PLAYER_CLASSES = [
  {
    id: 'Guerrero',
    name: 'Guerrero',
    icon: '⚔️',
    description: 'Especialista en combate físico y resistencia suprema.',
    stats: 'Fuerza · Vida · Estamina',
    image: guerrero,
  },
  {
    id: 'Arquero',
    name: 'Arquero',
    icon: '🏹',
    description: 'Maestro de la velocidad, precisión y agilidad en combate.',
    stats: 'Velocidad · Precisión · Destreza',
    image: arquero,
  },
  {
    id: 'Mago',
    name: 'Mago',
    icon: '🪄',
    description: 'Experto en conocimiento arcano, magia y sabiduría.',
    stats: 'Conocimiento · Magia · IQ',
    image: mago,
  },
]

export const DEFAULT_PLAYER_CLASS = PLAYER_CLASSES[0].id

export const CLASS_ICONS = [
  '⚔️',
  '🛡️',
  '🏹',
  '💧',
  '✨',
  '💚',
  '🎯',
  '🐉',
  '🔥',
  '🗡️',
  '🪄',
  '💎',
  '👑',
  '☀️',
  '⚡',
  '⛰️',
  '🐺',
  '🦊',
]

export function classById(id) {
  return PLAYER_CLASSES.find((item) => item.id === id) ?? PLAYER_CLASSES[0]
}
