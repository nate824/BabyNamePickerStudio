# Kindred Baby Names

Swipe on baby names with your partner on two Android phones. Names you both like become shared
matches; Claude suggests new names, explains your taste, and does deep dives on favorites.

## How it fits together

```
 phone A ─┐   HTTPS + WebSocket    ┌─ Caddy (babynames.n8ai.io) ─ kindred-sync (Docker, :4180) ─ SQLite
 phone B ─┘ ─────────────────────► │                                  │
                                                                      └─ Claude API (key lives here only)
```

- `app/` — Android app (Kotlin, Compose, Room). Each phone keeps a local copy of everything so swiping is
  instant and works offline; changes queue in a `pending_ops` outbox and sync when online. While the app is
  open, a WebSocket tells it when the partner changed something.
- `server/` — Node/TypeScript sync server (Fastify + SQLite). It owns pairing, decides matches, and makes
  all Claude calls, so the API key never ships inside the APK.

## Server

Deployed at `/opt/kindred` on the VPS, behind the `babynames.n8ai.io` block in `/etc/caddy/Caddyfile`.

```bash
# set or change the Claude API key
ssh root@187.124.147.150
nano /opt/kindred/.env            # ANTHROPIC_API_KEY=sk-ant-...
cd /opt/kindred && docker compose up -d   # restart to pick it up

# redeploy after code changes (from this repo)
rsync -az --delete --exclude node_modules --exclude dist --exclude data --exclude .env \
  server/ root@187.124.147.150:/opt/kindred/
ssh root@187.124.147.150 'cd /opt/kindred && docker compose up -d --build'

# logs / backup
ssh root@187.124.147.150 'docker logs -f kindred-sync'
scp root@187.124.147.150:/opt/kindred/data/kindred.db ./kindred-backup.db
```

`.env` options: `CLAUDE_MODEL` (default `claude-opus-5`), `CLAUDE_EFFORT` (`low`/`medium`/`high`,
default `medium`), `AI_DAILY_LIMIT` (AI calls per couple per day, default 30).

Local dev: `cd server && npm install && npm run dev` (listens on :4180), `npm test` for the test suite.

## App

```bash
./gradlew :app:assembleRelease          # → app/build/outputs/apk/release/app-release.apk
./gradlew :app:assembleDebug -PserverUrl=http://10.0.2.2:4180   # emulator against a local server
./gradlew :app:testDebugUnitTest        # add SYNC_E2E_URL=http://127.0.0.1:4180 to run the two-phone sync test
```

Release builds are signed with `keystore/kindred-release.jks` (passwords in `keystore.properties`, both
git-ignored). **Back these up** — Android only installs updates signed with the same key.

Install: copy the APK to each phone and open it (allow "install unknown apps" for your file manager or
browser when prompted), or `adb install app-release.apk` over USB.

## Pairing

First launch asks for your name, then one person taps **Create a pair code** and the other enters it.
Linking later, showing the code again, renaming, and unlinking are all in the link chip on the top bar.
