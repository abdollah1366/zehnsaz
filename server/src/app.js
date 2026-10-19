const express = require('express')
const { hashPassword, verifyPassword, newSessionToken, hashToken } = require('./auth')

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const MIN_PASSWORD_LENGTH = 8

/**
 * Builds the HTTP app. Accounts are always created as patients. A doctor role is
 * given only by an administrator (see src/cli/make-doctor.js), never by sign-up.
 */
function createApp({ pool, sessionDays }) {
  const app = express()
  app.use(express.json({ limit: '10kb' }))

  app.get('/api/health', (_req, res) => {
    res.json({ ok: true })
  })

  app.post('/api/register', async (req, res, next) => {
    try {
      const email = normalizeEmail(req.body?.email)
      const password = req.body?.password
      if (!email || !EMAIL_PATTERN.test(email)) return sendError(res, 400, 'invalid_email')
      if (typeof password !== 'string' || password.length < MIN_PASSWORD_LENGTH) {
        return sendError(res, 400, 'password_too_short')
      }

      const result = await pool.query(
        'INSERT INTO users (email, password_hash) VALUES ($1, $2) ON CONFLICT (email) DO NOTHING RETURNING id, email, role',
        [email, hashPassword(password)]
      )
      if (result.rowCount === 0) return sendError(res, 409, 'email_taken')
      res.status(201).json({ user: result.rows[0] })
    } catch (error) {
      next(error)
    }
  })

  app.post('/api/login', async (req, res, next) => {
    try {
      const email = normalizeEmail(req.body?.email)
      const password = req.body?.password
      if (!email || typeof password !== 'string') return sendError(res, 400, 'invalid_request')

      const result = await pool.query('SELECT id, email, role, password_hash FROM users WHERE email = $1', [email])
      const user = result.rows[0]
      // The same error for an unknown email and a wrong password, so the response does not reveal which accounts exist.
      if (!user || !verifyPassword(password, user.password_hash)) {
        return sendError(res, 401, 'invalid_credentials')
      }

      const { token, tokenHash } = newSessionToken()
      await pool.query(
        "INSERT INTO sessions (token_hash, user_id, expires_at) VALUES ($1, $2, now() + make_interval(days => $3))",
        [tokenHash, user.id, sessionDays]
      )
      res.json({ token, user: { id: user.id, email: user.email, role: user.role } })
    } catch (error) {
      next(error)
    }
  })

  app.post('/api/logout', requireSession(pool), async (req, res, next) => {
    try {
      await pool.query('DELETE FROM sessions WHERE token_hash = $1', [req.sessionTokenHash])
      res.status(204).end()
    } catch (error) {
      next(error)
    }
  })

  app.get('/api/me', requireSession(pool), (req, res) => {
    res.json({ user: req.user })
  })

  app.use((error, _req, res, _next) => {
    console.error(error)
    sendError(res, 500, 'server_error')
  })

  return app
}

/** Reads the bearer token, checks it is unexpired, and attaches the user to the request. */
function requireSession(pool) {
  return async (req, res, next) => {
    try {
      const header = req.get('authorization') || ''
      const token = header.startsWith('Bearer ') ? header.slice(7) : ''
      if (!token) return sendError(res, 401, 'not_signed_in')

      const tokenHash = hashToken(token)
      const result = await pool.query(
        `SELECT users.id, users.email, users.role
           FROM sessions JOIN users ON users.id = sessions.user_id
          WHERE sessions.token_hash = $1 AND sessions.expires_at > now()`,
        [tokenHash]
      )
      if (result.rowCount === 0) return sendError(res, 401, 'not_signed_in')

      req.user = result.rows[0]
      req.sessionTokenHash = tokenHash
      next()
    } catch (error) {
      next(error)
    }
  }
}

function normalizeEmail(value) {
  return typeof value === 'string' ? value.trim().toLowerCase() : ''
}

function sendError(res, status, code) {
  res.status(status).json({ error: code })
}

module.exports = { createApp }
