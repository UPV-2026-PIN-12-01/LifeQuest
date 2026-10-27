import { chromium } from 'playwright-core'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const dir = path.dirname(fileURLToPath(import.meta.url))
const jpegPath = path.join(dir, 'fixtures', 'avatar.jpg')
const baseUrl = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:5173'
const stamp = Date.now()
const photoUser = `test-can-delete-later-f3p-${stamp}`
const skipUser = `test-can-delete-later-f3s-${stamp}`
const photoHero = `test-can-delete-later-hero-${stamp}`
const skipHero = `test-can-delete-later-skip-${stamp}`
const results = []

function check(name, ok, extra = '') {
  results.push(`${ok ? 'PASS' : 'FAIL'} ${name}${extra ? ` — ${extra}` : ''}`)
}

async function fillAccount(page, { username, email, password, photo }) {
  await page.locator('input[name="username"]').fill(username)
  await page.locator('input[name="email"]').fill(email)
  await page.locator('input[name="password"]').fill(password)
  if (photo) {
    await page.locator('input[type="file"]').setInputFiles(photo)
  }
}

async function goToCharacter(page) {
  await page.getByRole('button', { name: 'Siguiente → Crear personaje' }).click()
  await page.getByRole('heading', { name: 'Crea tu personaje' }).waitFor()
}

const browser = await chromium.launch({
  channel: process.env.PLAYWRIGHT_CHANNEL || 'msedge',
  headless: true,
})
const page = await browser.newPage({ viewport: { width: 1280, height: 800 } })
const signupCalls = []
page.on('request', (request) => {
  if (request.url().includes('/api/auth/signup') && request.method() === 'POST') {
    signupCalls.push({ url: request.url() })
  }
})
page.on('response', async (response) => {
  if (
    response.url().includes('/api/auth/signup') &&
    response.request().method() === 'POST'
  ) {
    const last = signupCalls.at(-1)
    if (last && last.status == null) {
      last.status = response.status()
      try {
        last.body = await response.json()
      } catch {
        last.body = null
      }
    }
  }
})

try {
  await page.goto(baseUrl, { waitUntil: 'networkidle' })

  const beforeShortPassword = signupCalls.length
  await fillAccount(page, {
    username: photoUser,
    email: `${photoUser}@lifequest.test`,
    password: '123',
  })
  await page.getByRole('button', { name: 'Siguiente → Crear personaje' }).click()
  const passwordError = await page.locator('.signup-error-tooltip').innerText()
  const stillAccount = await page
    .getByRole('heading', { name: 'Crea tu cuenta' })
    .isVisible()
  check(
    'password 123 never hits API',
    stillAccount &&
      passwordError.includes('6') &&
      signupCalls.length === beforeShortPassword,
    `error="${passwordError}" hits=${signupCalls.length - beforeShortPassword}`,
  )

  await fillAccount(page, {
    username: photoUser,
    email: `${photoUser}@lifequest.test`,
    password: 'secret1',
    photo: jpegPath,
  })
  await goToCharacter(page)
  await page.locator('input[name="playerName"]').fill(photoHero)
  const photoStart = signupCalls.length
  await page.getByRole('button', { name: '✨ Empezar aventura' }).click()
  await page.waitForURL('**/home', { timeout: 30000 })
  const photoCall = signupCalls[photoStart]
  check(
    'photo 201 goes home',
    photoCall?.status === 201 &&
      photoCall?.body?.username === photoUser &&
      page.url().endsWith('/home'),
    `status=${photoCall?.status} body=${JSON.stringify(photoCall?.body)}`,
  )

  await page.goto(baseUrl, { waitUntil: 'networkidle' })
  await fillAccount(page, {
    username: skipUser,
    email: `${skipUser}@lifequest.test`,
    password: 'secret1',
  })
  await goToCharacter(page)
  await page.locator('input[name="playerName"]').fill(skipHero)
  const skipStart = signupCalls.length
  await page.getByRole('button', { name: '✨ Empezar aventura' }).click()
  await page.waitForURL('**/home', { timeout: 30000 })
  const skipCall = signupCalls[skipStart]
  check(
    'skip photo 201 goes home',
    skipCall?.status === 201 &&
      skipCall?.body?.username === skipUser &&
      page.url().endsWith('/home'),
    `status=${skipCall?.status} body=${JSON.stringify(skipCall?.body)}`,
  )

  await page.goto(baseUrl, { waitUntil: 'networkidle' })
  await fillAccount(page, {
    username: photoUser,
    email: `${photoUser}@lifequest.test`,
    password: 'secret1',
  })
  await goToCharacter(page)
  await page.locator('input[name="playerName"]').fill(`${photoHero}-dup`)
  const dupStart = signupCalls.length
  await page.getByRole('button', { name: '✨ Empezar aventura' }).click()
  await page.getByRole('heading', { name: 'Crea tu cuenta' }).waitFor({ timeout: 30000 })
  const dupError = await page.locator('.signup-error-tooltip').innerText()
  const dupCall = signupCalls[dupStart]
  check(
    'duplicate 409 stays on signup',
    dupCall?.status === 409 &&
      dupCall?.body?.error === 'Username already exists' &&
      !page.url().endsWith('/home') &&
      /nombre de usuario ya existe/i.test(dupError),
    `status=${dupCall?.status} body=${JSON.stringify(dupCall?.body)} error="${dupError}"`,
  )
} catch (error) {
  check('script', false, error.stack || String(error))
} finally {
  console.log(`USERS photo=${photoUser} skip=${skipUser}`)
  console.log(`HEROES photo=${photoHero} skip=${skipHero}`)
  console.log(results.join('\n'))
  await browser.close()
  if (results.some((line) => line.startsWith('FAIL'))) {
    process.exit(1)
  }
}
