const { loadConfig } = require('./config')
const { createPool, migrate } = require('./db')
const { createApp } = require('./app')

async function main() {
  const config = loadConfig()
  const pool = createPool(config.databaseUrl)
  await migrate(pool)

  const app = createApp({ pool, sessionDays: config.sessionDays })
  app.listen(config.port, config.host, () => {
    console.log(`Zehnsaz server listening on ${config.host}:${config.port}`)
  })
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
