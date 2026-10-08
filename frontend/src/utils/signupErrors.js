const SIGNUP_ERRORS = {
  'Username is required': 'El nombre de usuario es obligatorio',
  'Email is required': 'El email es obligatorio',
  'Invalid email': 'El email no es válido',
  'Password must be at least 6 characters':
    'La contraseña debe tener al menos 6 caracteres',
  'Username or email already exists':
    'El nombre de usuario o el email ya existe',
  'Username, email, or player name already exists':
    'El nombre de usuario, el email o el nombre del personaje ya existe',
  'Photo must be jpeg, png, webp, or gif and 2MB or smaller':
    'La foto debe ser jpeg, png, webp o gif y pesar 2 MB o menos',
  'Invalid photo': 'La foto no es válida',
}

export function signupErrorMessage(status, payload) {
  const english = payload?.error
  if (english && SIGNUP_ERRORS[english]) {
    return SIGNUP_ERRORS[english]
  }
  if (english && english.startsWith('Unknown player class')) {
    return 'La clase no es válida'
  }
  if (status === 409) {
    return 'Ese usuario ya existe'
  }
  if (status >= 500) {
    return 'Error del servidor. Inténtalo de nuevo.'
  }
  return 'No se pudo crear la cuenta. Inténtalo de nuevo.'
}
