import Database from "better-sqlite3";
import { createHash, randomBytes, randomUUID } from "node:crypto";

export type DB = Database.Database;

export interface Device {
  id: string;
  displayName: string;
  coupleId: string | null;
}

export interface SwipeRow {
  deviceId: string;
  nameId: string;
  liked: boolean;
  ts: number;
}

export interface MatchRow {
  nameId: string;
  matchedAt: number;
  rating: number;
  notes: string;
}

export interface CustomName {
  id: string;
  name: string;
  gender: "BOY" | "GIRL" | "UNISEX";
  origin: string;
  meaning: string;
  pronunciation: string;
  popularityRank: number;
  styleTags: string[];
  addedBy: string;
  source: "user" | "ai";
  createdAt: number;
}

// No 0/O or 1/I/L so codes survive being read aloud.
const CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

export function openDb(path: string): DB {
  const db = new Database(path);
  db.pragma("journal_mode = WAL");
  db.pragma("foreign_keys = ON");
  db.exec(`
    CREATE TABLE IF NOT EXISTS couples (
      id TEXT PRIMARY KEY,
      code TEXT NOT NULL UNIQUE,
      created_at INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS devices (
      id TEXT PRIMARY KEY,
      token_hash TEXT NOT NULL UNIQUE,
      display_name TEXT NOT NULL,
      couple_id TEXT REFERENCES couples(id),
      created_at INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS swipes (
      device_id TEXT NOT NULL REFERENCES devices(id),
      name_id TEXT NOT NULL,
      liked INTEGER NOT NULL,
      ts INTEGER NOT NULL,
      PRIMARY KEY (device_id, name_id)
    );
    CREATE TABLE IF NOT EXISTS matches (
      couple_id TEXT NOT NULL REFERENCES couples(id),
      name_id TEXT NOT NULL,
      matched_at INTEGER NOT NULL,
      rating INTEGER NOT NULL DEFAULT 5,
      notes TEXT NOT NULL DEFAULT '',
      dismissed INTEGER NOT NULL DEFAULT 0,
      PRIMARY KEY (couple_id, name_id)
    );
    CREATE TABLE IF NOT EXISTS custom_names (
      id TEXT PRIMARY KEY,
      added_by TEXT NOT NULL REFERENCES devices(id),
      name TEXT NOT NULL,
      gender TEXT NOT NULL,
      origin TEXT NOT NULL,
      meaning TEXT NOT NULL,
      pronunciation TEXT NOT NULL,
      popularity_rank INTEGER NOT NULL,
      style_tags TEXT NOT NULL,
      source TEXT NOT NULL,
      created_at INTEGER NOT NULL
    );
    CREATE TABLE IF NOT EXISTS ai_usage (
      scope_id TEXT NOT NULL,
      day TEXT NOT NULL,
      count INTEGER NOT NULL,
      PRIMARY KEY (scope_id, day)
    );
  `);
  return db;
}

export function hashToken(token: string): string {
  return createHash("sha256").update(token).digest("hex");
}

export function createDevice(db: DB, displayName: string): { device: Device; token: string } {
  const id = randomUUID();
  const token = randomBytes(32).toString("base64url");
  db.prepare(
    "INSERT INTO devices (id, token_hash, display_name, created_at) VALUES (?, ?, ?, ?)",
  ).run(id, hashToken(token), displayName, Date.now());
  return { device: { id, displayName, coupleId: null }, token };
}

export function deviceByToken(db: DB, token: string): Device | null {
  const row = db
    .prepare("SELECT id, display_name, couple_id FROM devices WHERE token_hash = ?")
    .get(hashToken(token)) as { id: string; display_name: string; couple_id: string | null } | undefined;
  return row ? { id: row.id, displayName: row.display_name, coupleId: row.couple_id } : null;
}

export function renameDevice(db: DB, deviceId: string, displayName: string): void {
  db.prepare("UPDATE devices SET display_name = ? WHERE id = ?").run(displayName, deviceId);
}

export function coupleMembers(db: DB, coupleId: string): Device[] {
  const rows = db
    .prepare("SELECT id, display_name, couple_id FROM devices WHERE couple_id = ? ORDER BY created_at")
    .all(coupleId) as { id: string; display_name: string; couple_id: string }[];
  return rows.map((r) => ({ id: r.id, displayName: r.display_name, coupleId: r.couple_id }));
}

export function coupleCode(db: DB, coupleId: string): string {
  return (db.prepare("SELECT code FROM couples WHERE id = ?").get(coupleId) as { code: string }).code;
}

function generateCode(): string {
  const bytes = randomBytes(6);
  let s = "";
  for (const b of bytes) s += CODE_ALPHABET[b % CODE_ALPHABET.length];
  return `${s.slice(0, 3)}-${s.slice(3)}`;
}

export function normalizeCode(input: string): string {
  const s = input.toUpperCase().replace(/[^A-Z0-9]/g, "");
  return s.length === 6 ? `${s.slice(0, 3)}-${s.slice(3)}` : s;
}

export function createCouple(db: DB, deviceId: string): string {
  const id = randomUUID();
  let code = generateCode();
  while (db.prepare("SELECT 1 FROM couples WHERE code = ?").get(code)) code = generateCode();
  db.transaction(() => {
    db.prepare("INSERT INTO couples (id, code, created_at) VALUES (?, ?, ?)").run(id, code, Date.now());
    db.prepare("UPDATE devices SET couple_id = ? WHERE id = ?").run(id, deviceId);
  })();
  return code;
}

export type JoinResult = { ok: true; coupleId: string } | { ok: false; reason: "not_found" | "full" };

export function joinCouple(db: DB, deviceId: string, rawCode: string): JoinResult {
  const row = db.prepare("SELECT id FROM couples WHERE code = ?").get(normalizeCode(rawCode)) as
    | { id: string }
    | undefined;
  if (!row) return { ok: false, reason: "not_found" };
  const members = coupleMembers(db, row.id).filter((m) => m.id !== deviceId);
  if (members.length >= 2) return { ok: false, reason: "full" };
  db.transaction(() => {
    db.prepare("UPDATE devices SET couple_id = ? WHERE id = ?").run(row.id, deviceId);
    recomputeMatches(db, row.id);
  })();
  return { ok: true, coupleId: row.id };
}

export function leaveCouple(db: DB, deviceId: string): void {
  db.prepare("UPDATE devices SET couple_id = NULL WHERE id = ?").run(deviceId);
}

/** Rebuild the match set from both members' swipes, keeping rating/notes of surviving matches. */
export function recomputeMatches(db: DB, coupleId: string): void {
  const members = coupleMembers(db, coupleId);
  if (members.length < 2) return;
  const [a, b] = members;
  const mutual = db
    .prepare(
      `SELECT s1.name_id AS name_id, MAX(s1.ts, s2.ts) AS ts
       FROM swipes s1 JOIN swipes s2 ON s1.name_id = s2.name_id
       WHERE s1.device_id = ? AND s2.device_id = ? AND s1.liked = 1 AND s2.liked = 1`,
    )
    .all(a.id, b.id) as { name_id: string; ts: number }[];
  const keep = new Set(mutual.map((m) => m.name_id));
  const existing = db.prepare("SELECT name_id FROM matches WHERE couple_id = ?").all(coupleId) as {
    name_id: string;
  }[];
  for (const e of existing) {
    if (!keep.has(e.name_id)) db.prepare("DELETE FROM matches WHERE couple_id = ? AND name_id = ?").run(coupleId, e.name_id);
  }
  const insert = db.prepare(
    "INSERT OR IGNORE INTO matches (couple_id, name_id, matched_at) VALUES (?, ?, ?)",
  );
  for (const m of mutual) insert.run(coupleId, m.name_id, m.ts);
}

export interface SwipeInput {
  nameId: string;
  liked: boolean;
  ts: number;
}

/**
 * Apply a batch of swipes (last write wins by ts) and return name ids that became
 * brand-new matches as a result.
 */
export function applySwipes(db: DB, device: Device, swipes: SwipeInput[]): string[] {
  return db.transaction(() => {
    const before = matchIds(db, device.coupleId);
    const upsert = db.prepare(
      `INSERT INTO swipes (device_id, name_id, liked, ts) VALUES (?, ?, ?, ?)
       ON CONFLICT(device_id, name_id) DO UPDATE SET liked = excluded.liked, ts = excluded.ts
       WHERE excluded.ts >= swipes.ts`,
    );
    for (const s of swipes) upsert.run(device.id, s.nameId, s.liked ? 1 : 0, s.ts);
    if (!device.coupleId) return [];
    recomputeMatches(db, device.coupleId);
    return [...matchIds(db, device.coupleId)].filter((id) => !before.has(id));
  })();
}

export function deleteSwipe(db: DB, device: Device, nameId: string): void {
  db.transaction(() => {
    db.prepare("DELETE FROM swipes WHERE device_id = ? AND name_id = ?").run(device.id, nameId);
    if (device.coupleId) recomputeMatches(db, device.coupleId);
  })();
}

function matchIds(db: DB, coupleId: string | null): Set<string> {
  if (!coupleId) return new Set();
  const rows = db.prepare("SELECT name_id FROM matches WHERE couple_id = ?").all(coupleId) as { name_id: string }[];
  return new Set(rows.map((r) => r.name_id));
}

export function updateMatch(
  db: DB,
  coupleId: string,
  nameId: string,
  patch: { rating?: number; notes?: string; dismissed?: boolean },
): boolean {
  const cur = db
    .prepare("SELECT rating, notes, dismissed FROM matches WHERE couple_id = ? AND name_id = ?")
    .get(coupleId, nameId) as { rating: number; notes: string; dismissed: number } | undefined;
  if (!cur) return false;
  db.prepare("UPDATE matches SET rating = ?, notes = ?, dismissed = ? WHERE couple_id = ? AND name_id = ?").run(
    patch.rating ?? cur.rating,
    patch.notes ?? cur.notes,
    patch.dismissed === undefined ? cur.dismissed : patch.dismissed ? 1 : 0,
    coupleId,
    nameId,
  );
  return true;
}

export function addCustomName(db: DB, n: CustomName): CustomName {
  db.prepare(
    `INSERT OR IGNORE INTO custom_names
     (id, added_by, name, gender, origin, meaning, pronunciation, popularity_rank, style_tags, source, created_at)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
  ).run(
    n.id,
    n.addedBy,
    n.name,
    n.gender,
    n.origin,
    n.meaning,
    n.pronunciation,
    n.popularityRank,
    JSON.stringify(n.styleTags),
    n.source,
    n.createdAt,
  );
  return n;
}

export interface State {
  me: { id: string; displayName: string };
  partner: { id: string; displayName: string } | null;
  pairCode: string | null;
  swipes: SwipeRow[];
  matches: MatchRow[];
  names: CustomName[];
}

export function getState(db: DB, device: Device): State {
  const members = device.coupleId ? coupleMembers(db, device.coupleId) : [device];
  const ids = members.map((m) => m.id);
  const placeholders = ids.map(() => "?").join(",");
  const partner = members.find((m) => m.id !== device.id) ?? null;

  const swipes = (
    db.prepare(`SELECT device_id, name_id, liked, ts FROM swipes WHERE device_id IN (${placeholders})`).all(...ids) as {
      device_id: string;
      name_id: string;
      liked: number;
      ts: number;
    }[]
  ).map((r) => ({ deviceId: r.device_id, nameId: r.name_id, liked: r.liked === 1, ts: r.ts }));

  const matches = device.coupleId
    ? (
        db
          .prepare(
            "SELECT name_id, matched_at, rating, notes FROM matches WHERE couple_id = ? AND dismissed = 0 ORDER BY matched_at DESC",
          )
          .all(device.coupleId) as { name_id: string; matched_at: number; rating: number; notes: string }[]
      ).map((r) => ({ nameId: r.name_id, matchedAt: r.matched_at, rating: r.rating, notes: r.notes }))
    : [];

  const names = (
    db
      .prepare(`SELECT * FROM custom_names WHERE added_by IN (${placeholders}) ORDER BY created_at`)
      .all(...ids) as Record<string, unknown>[]
  ).map(rowToName);

  return {
    me: { id: device.id, displayName: device.displayName },
    partner: partner ? { id: partner.id, displayName: partner.displayName } : null,
    pairCode: device.coupleId ? coupleCode(db, device.coupleId) : null,
    swipes,
    matches,
    names,
  };
}

function rowToName(r: Record<string, unknown>): CustomName {
  return {
    id: r.id as string,
    name: r.name as string,
    gender: r.gender as CustomName["gender"],
    origin: r.origin as string,
    meaning: r.meaning as string,
    pronunciation: r.pronunciation as string,
    popularityRank: r.popularity_rank as number,
    styleTags: JSON.parse(r.style_tags as string) as string[],
    addedBy: r.added_by as string,
    source: r.source as CustomName["source"],
    createdAt: r.created_at as number,
  };
}

/** Count one AI call against today's quota; returns false once the limit is hit. */
export function consumeAiQuota(db: DB, scopeId: string, limit: number): boolean {
  const day = new Date().toISOString().slice(0, 10);
  return db.transaction(() => {
    const row = db.prepare("SELECT count FROM ai_usage WHERE scope_id = ? AND day = ?").get(scopeId, day) as
      | { count: number }
      | undefined;
    if ((row?.count ?? 0) >= limit) return false;
    db.prepare(
      `INSERT INTO ai_usage (scope_id, day, count) VALUES (?, ?, 1)
       ON CONFLICT(scope_id, day) DO UPDATE SET count = count + 1`,
    ).run(scopeId, day);
    return true;
  })();
}
