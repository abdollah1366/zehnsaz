// Usage: npm run make-doctor -- someone@example.com
// Gives an existing account the doctor role. Sign-up never grants this role.
const { loadConfig } = require('../config')
const { createPool, migrate } = require('../db')

async function main() {
  const email = (process.argv[2] || '').trim().toLowerCase()
  if (!email) throw new Error('Pass the account email, for example: npm run make-doctor -- doctor@example.com')

  const pool = createPool(loadConfig().databaseUrl)
  await migrate(pool)
  const result = await pool.query("UPDATE users SET role = 'doctor' WHERE email = $1 RETURNING email, role", [email])
  await pool.end()

  if (result.rowCount === 0) throw new Error(`No account with email ${email}`)
  console.log(`${result.rows[0].email} is now a doctor`)
}

main().catch((error) => {
  console.error(error.message)
  process.exit(1)
})
