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

## Tests

```
set TEST_DATABASE_URL=postgres://zehnsaz:CHANGE_ME@localhost:5432/zehnsaz
npm test
```

The tests clear the `users` table, so point them at a test database and never at real data.
