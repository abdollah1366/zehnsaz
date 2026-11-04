const express = require('express')
const { hashPassword, verifyPassword, newSessionToken, hashToken } = require('./auth')
const { BazaarUnavailableError } = require('./bazaar')

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const MIN_PASSWORD_LENGTH = 8
// Bazaar product ids and purchase tokens are short ASCII strings; anything else is refused before calling Bazaar.
const PRODUCT_ID_PATTERN = /^[A-Za-z0-9._-]{1,100}$/
const PURCHASE_TOKEN_PATTERN = /^[A-Za-z0-9._:-]{1,500}$/

/**
 * Builds the HTTP app. Accounts are always created as patients. A doctor role is
 * given only by an administrator (see src/cli/make-doctor.js), never by sign-up.
 */
function createApp({ pool, sessionDays, bazaar = null }) {
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

  /**
   * Checks a Bazaar purchase for the signed-in user.
   * Body: { type: "product" | "subscription", productId, purchaseToken }.
   * Answers { valid: true, ... } or { valid: false, reason }, so the app knows whether to deliver.
   */
  app.post('/api/billing/bazaar/verify', requireSession(pool), async (req, res, next) => {
    try {
      if (!bazaar) return sendError(res, 503, 'billing_not_configured')
      const { type, productId, purchaseToken } = req.body || {}
      if (type !== 'product' && type !== 'subscription') return sendError(res, 400, 'invalid_type')
      if (typeof productId !== 'string' || !PRODUCT_ID_PATTERN.test(productId)) return sendError(res, 400, 'invalid_product_id')
      if (typeof purchaseToken !== 'string' || !PURCHASE_TOKEN_PATTERN.test(purchaseToken)) {
        return sendError(res, 400, 'invalid_purchase_token')
      }

      let check
      try {
        check = type === 'product'
          ? await bazaar.verifyProduct(productId, purchaseToken)
          : await bazaar.verifySubscription(productId, purchaseToken)
      } catch (error) {
        if (error instanceof BazaarUnavailableError) {
          console.warn('Bazaar check failed:', error.message)
          return sendError(res, 502, 'store_unavailable')
        }
        throw error
      }
      if (!check.valid) return res.json({ valid: false, reason: check.reason })

      const validUntil = check.validUntil ? new Date(check.validUntil) : null
      await pool.query(
        `INSERT INTO store_purchases (purchase_token, kind, product_id, user_id, valid_until)
              VALUES ($1, $2, $3, $4, $5)
         ON CONFLICT (purchase_token) DO UPDATE SET valid_until = EXCLUDED.valid_until, verified_at = now()
               WHERE store_purchases.user_id = EXCLUDED.user_id`,
        [purchaseToken, type, productId, req.user.id, validUntil]
      )
      const owner = await pool.query('SELECT user_id, product_id FROM store_purchases WHERE purchase_token = $1', [purchaseToken])
      const row = owner.rows[0]
      if (row.user_id !== req.user.id) return res.json({ valid: false, reason: 'used_by_another_account' })
      if (row.product_id !== productId) return res.json({ valid: false, reason: 'product_mismatch' })

      res.json({ valid: true, ...check })
    } catch (error) {
      next(error)
    }
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
