export async function signup(formData) {
  const response = await fetch('/api/auth/signup', {
    method: 'POST',
    body: formData,
  })
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
