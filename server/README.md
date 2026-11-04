# Zehnsaz server

Accounts, sessions and roles for the app and the doctor panel. This first slice has sign-up,
login, logout, and "who am I". Patient data, invite codes, and the doctor panel come next.

Stack: Node.js 20 or newer, Express 4, PostgreSQL 14 or newer.

## Run it locally

```
npm ci
set DATABASE_URL=postgres://zehnsaz:CHANGE_ME@localhost:5432/zehnsaz
npm start
```

`GET /api/health` should return `{"ok":true}`.

## Make someone a doctor

Sign-up always creates a patient. Doctors are added by an administrator on the server:

```
npm run make-doctor -- doctor@example.com
```

## Set up on a Windows VPS

1. Install Node.js 22 LTS (the Windows installer) and PostgreSQL (the EDB installer).
2. Create the database and user in `psql`:
   `CREATE USER zehnsaz WITH PASSWORD 'CHANGE_ME';` then `CREATE DATABASE zehnsaz OWNER zehnsaz;`
3. Copy this `server` folder to the VPS, then run `npm ci`.
4. Set `DATABASE_URL` and `HOST=127.0.0.1` (only the reverse proxy below talks to the app), then `npm start`.
   To keep it running after a reboot, register it as a Windows service (for example with NSSM) or a scheduled task.
5. Put HTTPS in front of it. Passwords must not travel in plain HTTP from a phone. A simple option on Windows is Caddy,
   with a Caddyfile like this for your domain:

   ```
   zehnsaz.example.com {
       reverse_proxy 127.0.0.1:3000
   }
   ```

   Caddy gets the certificate automatically once the domain points to the VPS's public IP.
6. Open only the web ports in the firewall, from an administrator PowerShell:

   ```
   New-NetFirewallRule -DisplayName "Zehnsaz HTTPS" -Direction Inbound -Protocol TCP -LocalPort 80,443 -Action Allow
   ```

   Do not open PostgreSQL (5432) or the Node port (3000) to the internet.

## Check Bazaar purchases

`POST /api/billing/bazaar/verify` (signed in) asks Cafe Bazaar whether a purchase token is genuine and ties it to
the account, so one payment cannot unlock several accounts. Body:

```
{ "type": "product", "productId": "coins_100", "purchaseToken": "..." }
{ "type": "subscription", "productId": "<subscription id>", "purchaseToken": "..." }
```

It answers `{ "valid": true, ... }` or `{ "valid": false, "reason": "not_found" | "refunded" | "expired" | "used_by_another_account" | "product_mismatch" }`,
`502 store_unavailable` when Bazaar does not answer, and `503 billing_not_configured` when the key below is missing.

Settings (environment variables):

- `BAZAAR_API_SECRET`: the developer API key from the Bazaar panel (pishkhan.cafebazaar.ir > Developer API). Keep it on the server only.
- `BAZAAR_PACKAGE_NAME`: defaults to `com.barati.zehnsaz`.

## Tests

```
set TEST_DATABASE_URL=postgres://zehnsaz:CHANGE_ME@localhost:5432/zehnsaz
npm test
```

The tests clear the `users` table, so point them at a test database and never at real data.
