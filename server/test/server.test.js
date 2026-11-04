const { test, before, after, beforeEach } = require('node:test')
const assert = require('node:assert/strict')
const http = require('node:http')
const { createPool, migrate } = require('../src/db')
const { createApp } = require('../src/app')
const { BazaarUnavailableError } = require('../src/bazaar')

const databaseUrl = process.env.TEST_DATABASE_URL || process.env.DATABASE_URL
let pool
let server
let baseUrl

// Stands in for the Bazaar developer API. Each test sets the next answer.
const fakeBazaar = {
  next: { valid: true },
  async verifyProduct() {
    if (this.next instanceof Error) throw this.next
    return this.next
  },
  async verifySubscription() {
    if (this.next instanceof Error) throw this.next
    return this.next
  }
}

async function call(method, path, { body, token } = {}) {
  const headers = { 'content-type': 'application/json' }
  if (token) headers.authorization = `Bearer ${token}`
  const response = await fetch(baseUrl + path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body)
  })
  const text = await response.text()
  return { status: response.status, body: text ? JSON.parse(text) : null }
}

before(async () => {
  pool = createPool(databaseUrl)
  await migrate(pool)
  const app = createApp({ pool, sessionDays: 30, bazaar: fakeBazaar })
  server = http.createServer(app)
  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve))
  baseUrl = `http://127.0.0.1:${server.address().port}`
})

beforeEach(async () => {
  await pool.query('TRUNCATE users CASCADE')
  fakeBazaar.next = { valid: true }
})

after(async () => {
  await new Promise((resolve) => server.close(resolve))
  await pool.end()
})

test('health endpoint answers ok', async () => {
  const response = await call('GET', '/api/health')
  assert.equal(response.status, 200)
  assert.deepEqual(response.body, { ok: true })
})

test('sign-up creates a patient and stores no plain password', async () => {
  const response = await call('POST', '/api/register', {
    body: { email: ' Ali@Example.com ', password: 'correct horse' }
  })
  assert.equal(response.status, 201)
  assert.deepEqual(response.body.user, { id: response.body.user.id, email: 'ali@example.com', role: 'patient' })

  const stored = await pool.query('SELECT password_hash FROM users WHERE email = $1', ['ali@example.com'])
  assert.notEqual(stored.rows[0].password_hash, 'correct horse')
  assert.ok(!stored.rows[0].password_hash.includes('correct horse'))
})

test('sign-up ignores a requested doctor role', async () => {
  const response = await call('POST', '/api/register', {
    body: { email: 'fake@example.com', password: 'correct horse', role: 'doctor' }
  })
  assert.equal(response.body.user.role, 'patient')
})

test('a duplicate email is refused', async () => {
  await call('POST', '/api/register', { body: { email: 'a@example.com', password: 'correct horse' } })
  const response = await call('POST', '/api/register', { body: { email: 'A@example.com', password: 'another one' } })
  assert.equal(response.status, 409)
  assert.equal(response.body.error, 'email_taken')
})

test('a short password and a bad email are refused', async () => {
  const short = await call('POST', '/api/register', { body: { email: 'b@example.com', password: 'short' } })
  assert.equal(short.body.error, 'password_too_short')
  const badEmail = await call('POST', '/api/register', { body: { email: 'not-an-email', password: 'correct horse' } })
  assert.equal(badEmail.body.error, 'invalid_email')
})

test('login returns a token that opens /api/me, and logout ends it', async () => {
  await call('POST', '/api/register', { body: { email: 'c@example.com', password: 'correct horse' } })

  const login = await call('POST', '/api/login', { body: { email: 'c@example.com', password: 'correct horse' } })
  assert.equal(login.status, 200)

  const me = await call('GET', '/api/me', { token: login.body.token })
  assert.equal(me.status, 200)
  assert.equal(me.body.user.email, 'c@example.com')

  const logout = await call('POST', '/api/logout', { token: login.body.token })
  assert.equal(logout.status, 204)
  const after = await call('GET', '/api/me', { token: login.body.token })
  assert.equal(after.status, 401)
})

test('wrong password and unknown email give the same error', async () => {
  await call('POST', '/api/register', { body: { email: 'd@example.com', password: 'correct horse' } })
  const wrongPassword = await call('POST', '/api/login', { body: { email: 'd@example.com', password: 'wrong one' } })
  const unknownEmail = await call('POST', '/api/login', { body: { email: 'x@example.com', password: 'wrong one' } })
  assert.equal(wrongPassword.status, 401)
  assert.deepEqual(wrongPassword.body, unknownEmail.body)
})

test('requests without a valid token are refused', async () => {
  const none = await call('GET', '/api/me')
  assert.equal(none.status, 401)
  const bogus = await call('GET', '/api/me', { token: 'not-a-real-token' })
  assert.equal(bogus.status, 401)
})

test('an expired session is refused', async () => {
  await call('POST', '/api/register', { body: { email: 'e@example.com', password: 'correct horse' } })
  const login = await call('POST', '/api/login', { body: { email: 'e@example.com', password: 'correct horse' } })
  await pool.query("UPDATE sessions SET expires_at = now() - interval '1 second'")
  const me = await call('GET', '/api/me', { token: login.body.token })
  assert.equal(me.status, 401)
})

test('only the hash of a session token is stored', async () => {
  await call('POST', '/api/register', { body: { email: 'f@example.com', password: 'correct horse' } })
  const login = await call('POST', '/api/login', { body: { email: 'f@example.com', password: 'correct horse' } })
  const stored = await pool.query('SELECT token_hash FROM sessions')
  assert.equal(stored.rowCount, 1)
  assert.notEqual(stored.rows[0].token_hash, login.body.token)
})

async function signedIn(email) {
  await call('POST', '/api/register', { body: { email, password: 'correct horse' } })
  const login = await call('POST', '/api/login', { body: { email, password: 'correct horse' } })
  return login.body.token
}

test('a genuine Bazaar purchase is confirmed and tied to the account', async () => {
  const token = await signedIn('buyer@example.com')
  fakeBazaar.next = { valid: true, consumed: false, purchaseTime: 1, developerPayload: null }

  const response = await call('POST', '/api/billing/bazaar/verify', {
    token,
    body: { type: 'product', productId: 'coins_100', purchaseToken: 'tok_1' }
  })

  assert.equal(response.status, 200)
  assert.equal(response.body.valid, true)
  const stored = await pool.query('SELECT product_id, kind FROM store_purchases WHERE purchase_token = $1', ['tok_1'])
  assert.deepEqual(stored.rows[0], { product_id: 'coins_100', kind: 'product' })

  // Checking the same purchase again from the same account is fine.
  const again = await call('POST', '/api/billing/bazaar/verify', {
    token,
    body: { type: 'product', productId: 'coins_100', purchaseToken: 'tok_1' }
  })
  assert.equal(again.body.valid, true)
})

test('one purchase token cannot unlock a second account', async () => {
  const first = await signedIn('first@example.com')
  const second = await signedIn('second@example.com')
  const body = { type: 'subscription', productId: 'monthly', purchaseToken: 'tok_shared' }
  fakeBazaar.next = { valid: true, validUntil: Date.now() + 86_400_000, autoRenewing: true }

  assert.equal((await call('POST', '/api/billing/bazaar/verify', { token: first, body })).body.valid, true)
  const response = await call('POST', '/api/billing/bazaar/verify', { token: second, body })

  assert.deepEqual(response.body, { valid: false, reason: 'used_by_another_account' })
})

test('a purchase Bazaar does not know is refused and not stored', async () => {
  const token = await signedIn('buyer@example.com')
  fakeBazaar.next = { valid: false, reason: 'not_found' }

  const response = await call('POST', '/api/billing/bazaar/verify', {
    token,
    body: { type: 'product', productId: 'coins_100', purchaseToken: 'forged' }
  })

  assert.deepEqual(response.body, { valid: false, reason: 'not_found' })
  const stored = await pool.query('SELECT 1 FROM store_purchases')
  assert.equal(stored.rowCount, 0)
})

test('purchase check: bad input, no session, and Bazaar down', async () => {
  const token = await signedIn('buyer@example.com')
  const good = { type: 'product', productId: 'coins_100', purchaseToken: 'tok' }

  assert.equal((await call('POST', '/api/billing/bazaar/verify', { body: good })).status, 401)
  assert.equal((await call('POST', '/api/billing/bazaar/verify', { token, body: { ...good, type: 'gift' } })).status, 400)
  assert.equal((await call('POST', '/api/billing/bazaar/verify', { token, body: { ...good, productId: '../x' } })).status, 400)
  assert.equal((await call('POST', '/api/billing/bazaar/verify', { token, body: { ...good, purchaseToken: '' } })).status, 400)

  fakeBazaar.next = new BazaarUnavailableError('down')
  const down = await call('POST', '/api/billing/bazaar/verify', { token, body: good })
  assert.deepEqual(down, { status: 502, body: { error: 'store_unavailable' } })
})
