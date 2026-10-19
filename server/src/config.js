/** Settings come from environment variables so the same code runs on the VPS and in tests. */
function loadConfig(env = process.env) {
  const databaseUrl = env.DATABASE_URL
  if (!databaseUrl) {
    throw new Error('DATABASE_URL is required, for example postgres://user:password@localhost:5432/zehnsaz')
  }
  return {
    databaseUrl,
    host: env.HOST || '0.0.0.0',
    port: Number(env.PORT || 3000),
    sessionDays: Number(env.SESSION_DAYS || 30)
  }
}

module.exports = { loadConfig }
