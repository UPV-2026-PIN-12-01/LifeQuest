export async function signup(formData) {
  let response
  try {
    response = await fetch('/api/auth/signup', {
      method: 'POST',
      body: formData,
    })
  } catch {
    const error = new Error('Network error')
    error.status = 0
    error.payload = { error: 'Network error' }
    throw error
  }
  let payload = null
  try {
    payload = await response.json()
  } catch {
    payload = null
  }
  if (!response.ok) {
    const error = new Error(payload?.error || 'Signup failed')
    error.status = response.status
    error.payload = payload
    throw error
  }
  return payload
}
