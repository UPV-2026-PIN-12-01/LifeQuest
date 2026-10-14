// Keep MIN_PASSWORD_LENGTH and EMAIL_SHAPE in sync with SignupService.java
export const MIN_PASSWORD_LENGTH = 6
export const EMAIL_SHAPE = /^[^@\s]+@[^@\s]+\.[^@\s]+$/

// Keep MAX_PHOTO_BYTES and photo types in sync with SignupService.java
export const MAX_PHOTO_BYTES = 2 * 1024 * 1024
const PHOTO_TYPES = new Set([
  'image/jpeg',
  'image/jpg',
  'image/png',
  'image/webp',
  'image/gif',
])

export function validateAccount({ username, email, password }) {
  const errors = {}
  if (!username.trim()) {
    errors.username = 'El nombre de usuario es obligatorio'
  }
  const normalizedEmail = email.trim().toLowerCase()
  if (!normalizedEmail) {
    errors.email = 'El email es obligatorio'
  } else if (!EMAIL_SHAPE.test(normalizedEmail)) {
    errors.email = 'El email no es válido'
  }
  if (!password || password.length < MIN_PASSWORD_LENGTH) {
    errors.password = `La contraseña debe tener al menos ${MIN_PASSWORD_LENGTH} caracteres`
  }
  return errors
}

export function validatePhoto(file) {
  if (!file) {
    return ''
  }
  if (file.size > MAX_PHOTO_BYTES || !isAllowedPhoto(file)) {
    return 'La foto debe ser jpeg, png, webp o gif y pesar 2 MB o menos'
  }
  return ''
}

function isAllowedPhoto(file) {
  const type = (file.type || '').toLowerCase().split(';')[0].trim()
  const canonical = type === 'image/jpg' ? 'image/jpeg' : type
  if (PHOTO_TYPES.has(canonical)) {
    return true
  }
  const name = (file.name || '').toLowerCase()
  return (
    name.endsWith('.jpg') ||
    name.endsWith('.jpeg') ||
    name.endsWith('.png') ||
    name.endsWith('.webp') ||
    name.endsWith('.gif')
  )
}
