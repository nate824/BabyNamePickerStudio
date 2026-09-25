import { mkdirSync } from "node:fs";
import { dirname } from "node:path";
import { ClaudeAiService, DisabledAiService, type AiService } from "./ai.js";
import { buildApp } from "./app.js";
import { openDb } from "./db.js";

const dbPath = process.env.DB_PATH ?? "./data/kindred.db";
mkdirSync(dirname(dbPath), { recursive: true });

const apiKey = process.env.ANTHROPIC_API_KEY?.trim();
const effort = (process.env.CLAUDE_EFFORT ?? "medium") as "low" | "medium" | "high";
const ai: AiService = apiKey
  ? new ClaudeAiService(apiKey, process.env.CLAUDE_MODEL ?? "claude-opus-5", effort)
  : new DisabledAiService();

const app = await buildApp({
  db: openDb(dbPath),
  ai,
  aiDailyLimit: Number(process.env.AI_DAILY_LIMIT ?? 30),
  logger: true,
});

if (!apiKey) app.log.warn("ANTHROPIC_API_KEY not set - AI features will return 503");

await app.listen({ host: process.env.HOST ?? "0.0.0.0", port: Number(process.env.PORT ?? 4180) });
