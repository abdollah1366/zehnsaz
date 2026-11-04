const { loadConfig } = require('./config')
const { createPool, migrate } = require('./db')
const { createApp } = require('./app')
const { createBazaarClient } = require('./bazaar')

async function main() {
  const config = loadConfig()
  const pool = createPool(config.databaseUrl)
  await migrate(pool)

  const bazaar = config.bazaarApiSecret
    ? createBazaarClient({ apiSecret: config.bazaarApiSecret, packageName: config.bazaarPackageName })
    : null
  if (!bazaar) console.warn('BAZAAR_API_SECRET is not set: /api/billing/bazaar/verify will answer 503')

  const app = createApp({ pool, sessionDays: config.sessionDays, bazaar })
  app.listen(config.port, config.host, () => {
    console.log(`Zehnsaz server listening on ${config.host}:${config.port}`)
  })
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
