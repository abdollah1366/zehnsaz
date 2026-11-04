const { Pool } = require('pg')

const SCHEMA = `
CREATE TABLE IF NOT EXISTS users (
  id            SERIAL PRIMARY KEY,
  email         TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role          TEXT NOT NULL DEFAULT 'patient' CHECK (role IN ('patient', 'doctor')),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sessions (
  token_hash TEXT PRIMARY KEY,
  user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  expires_at TIMESTAMPTZ NOT NULL
);

-- Bazaar purchase tokens that passed the server check. A token belongs to the first account that
-- verified it, so one payment cannot unlock several accounts.
CREATE TABLE IF NOT EXISTS store_purchases (
  purchase_token TEXT PRIMARY KEY,
  store          TEXT NOT NULL DEFAULT 'bazaar',
  kind           TEXT NOT NULL CHECK (kind IN ('product', 'subscription')),
  product_id     TEXT NOT NULL,
  user_id        INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  valid_until    TIMESTAMPTZ,
  verified_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
`

function createPool(databaseUrl) {
  return new Pool({ connectionString: databaseUrl })
}

/** Creates the tables if they do not exist yet. Safe to run on every start. */
async function migrate(pool) {
  await pool.query(SCHEMA)
}

module.exports = { createPool, migrate }
