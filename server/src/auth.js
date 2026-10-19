const crypto = require('node:crypto')

const SCRYPT_KEY_LENGTH = 64

/** Returns "salt$hash" as base64. A new random salt is used for every password. */
function hashPassword(password) {
  const salt = crypto.randomBytes(16)
  const hash = crypto.scryptSync(password, salt, SCRYPT_KEY_LENGTH)
  return `${salt.toString('base64')}$${hash.toString('base64')}`
}

function verifyPassword(password, stored) {
  const [saltB64, hashB64] = stored.split('$')
  if (!saltB64 || !hashB64) return false
  const expected = Buffer.from(hashB64, 'base64')
  const actual = crypto.scryptSync(password, Buffer.from(saltB64, 'base64'), expected.length)
  return crypto.timingSafeEqual(actual, expected)
}

/** A random session token for the client. Only its SHA-256 hash is stored, so a leaked table cannot be used. */
function newSessionToken() {
  const token = crypto.randomBytes(32).toString('base64url')
  return { token, tokenHash: hashToken(token) }
}

function hashToken(token) {
  return crypto.createHash('sha256').update(token).digest('hex')
}

module.exports = { hashPassword, verifyPassword, newSessionToken, hashToken }
