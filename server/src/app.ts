import Anthropic from "@anthropic-ai/sdk";
import websocket from "@fastify/websocket";
import Fastify, { type FastifyInstance, type FastifyReply, type FastifyRequest } from "fastify";
import { randomUUID } from "node:crypto";
import type { WebSocket } from "ws";
import { z } from "zod";
import {
  AiRefusedError,
  AiUnavailableError,
  NameBriefSchema,
  type AiService,
  type IdeaContext,
  type NameIdea,
} from "./ai.js";
import {
  addCustomName,
  applySwipes,
  consumeAiQuota,
  createCouple,
  createDevice,
  deleteSwipe,
  deviceByToken,
  getState,
  joinCouple,
  leaveCouple,
  renameDevice,
  updateMatch,
  type CustomName,
  type DB,
  type Device,
} from "./db.js";

export interface AppOptions {
  db: DB;
  ai: AiService;
  aiDailyLimit: number;
  logger?: boolean;
}

declare module "fastify" {
  interface FastifyRequest {
    device: Device;
  }
}

const Gender = z.enum(["BOY", "GIRL", "UNISEX"]);
const DisplayName = z.string().trim().min(1).max(40);

const IdeaRequest = z.object({
  liked: z.array(NameBriefSchema).max(300).default([]),
  disliked: z.array(NameBriefSchema).max(300).default([]),
  avoid: z.array(z.string().max(60)).max(5000).default([]),
  gender: Gender.nullable().default(null),
  count: z.number().int().min(1).max(20).default(10),
});

/** Tracks open sockets per couple (or per solo device) so changes can be pushed live. */
class Hub {
  private rooms = new Map<string, Set<WebSocket>>();

  join(room: string, ws: WebSocket): void {
    let set = this.rooms.get(room);
    if (!set) this.rooms.set(room, (set = new Set()));
    set.add(ws);
    ws.on("close", () => {
      set.delete(ws);
      if (set.size === 0) this.rooms.delete(room);
    });
  }

  notify(room: string): void {
    for (const ws of this.rooms.get(room) ?? []) {
      if (ws.readyState === ws.OPEN) ws.send(JSON.stringify({ type: "changed" }));
    }
  }
}

const roomOf = (d: Device) => d.coupleId ?? `device:${d.id}`;

export async function buildApp(opts: AppOptions): Promise<FastifyInstance> {
  const { db, ai } = opts;
  const app = Fastify({ logger: opts.logger ?? false, bodyLimit: 2 * 1024 * 1024 });
  const hub = new Hub();
  await app.register(websocket);

  app.setErrorHandler((err, _req, reply) => {
    if (err instanceof z.ZodError) return reply.code(400).send({ error: "invalid_request", detail: err.issues });
    if (err instanceof AiUnavailableError) return reply.code(503).send({ error: "ai_unavailable", detail: err.message });
    if (err instanceof AiRefusedError) return reply.code(422).send({ error: "ai_refused", detail: err.message });
    if (err instanceof Anthropic.RateLimitError) return reply.code(429).send({ error: "ai_rate_limited" });
    if (err instanceof Anthropic.AuthenticationError) {
      app.log.error("Anthropic rejected the API key");
      return reply.code(503).send({ error: "ai_unavailable", detail: "The server's Anthropic API key was rejected" });
    }
    if (err instanceof Anthropic.APIError) {
      app.log.error({ status: err.status }, `Anthropic API error: ${err.message}`);
      return reply.code(502).send({ error: "ai_failed" });
    }
    const status = (err as { statusCode?: number }).statusCode;
    if (status && status < 500) return reply.code(status).send({ error: (err as Error).message });
    app.log.error(err);
    return reply.code(500).send({ error: "internal" });
  });

  function authenticate(req: FastifyRequest, reply: FastifyReply, token: string | undefined): boolean {
    const device = token ? deviceByToken(db, token) : null;
    if (!device) {
      reply.code(401).send({ error: "unauthorized" });
      return false;
    }
    req.device = device;
    return true;
  }

  const auth = async (req: FastifyRequest, reply: FastifyReply) => {
    const header = req.headers.authorization;
    authenticate(req, reply, header?.startsWith("Bearer ") ? header.slice(7) : undefined);
  };

  const notify = (d: Device) => hub.notify(roomOf(d));

  const aiGuard = (req: FastifyRequest, reply: FastifyReply): boolean => {
    if (!consumeAiQuota(db, roomOf(req.device), opts.aiDailyLimit)) {
      reply.code(429).send({ error: "ai_quota", detail: `Daily limit of ${opts.aiDailyLimit} AI requests reached` });
      return false;
    }
    return true;
  };

  app.get("/api/health", async () => ({ ok: true }));

  app.post("/api/devices", async (req) => {
    const body = z.object({ displayName: DisplayName }).parse(req.body);
    const { device, token } = createDevice(db, body.displayName);
    return { deviceId: device.id, token };
  });

  app.register(async (authed) => {
    authed.addHook("preHandler", auth);

    authed.get("/api/state", async (req) => getState(db, req.device));

    authed.patch("/api/me", async (req) => {
      const body = z.object({ displayName: DisplayName }).parse(req.body);
      renameDevice(db, req.device.id, body.displayName);
      notify(req.device);
      return { ok: true };
    });

    authed.post("/api/couples", async (req, reply) => {
      if (req.device.coupleId) return reply.code(409).send({ error: "already_paired" });
      const code = createCouple(db, req.device.id);
      return { pairCode: code };
    });

    authed.post("/api/couples/join", async (req, reply) => {
      const body = z.object({ code: z.string().min(4).max(20) }).parse(req.body);
      const res = joinCouple(db, req.device.id, body.code);
      if (!res.ok) {
        return reply
          .code(res.reason === "not_found" ? 404 : 409)
          .send({ error: res.reason === "not_found" ? "code_not_found" : "couple_full" });
      }
      const oldRoom = roomOf(req.device);
      req.device.coupleId = res.coupleId;
      hub.notify(oldRoom);
      notify(req.device);
      return getState(db, req.device);
    });

    authed.post("/api/couples/leave", async (req) => {
      const before = { ...req.device };
      leaveCouple(db, req.device.id);
      notify(before);
      return { ok: true };
    });

    authed.put("/api/swipes", async (req) => {
      const body = z
        .object({
          swipes: z
            .array(z.object({ nameId: z.string().min(1).max(100), liked: z.boolean(), ts: z.number().int() }))
            .max(1000),
        })
        .parse(req.body);
      const newMatches = applySwipes(db, req.device, body.swipes);
      notify(req.device);
      return { newMatches };
    });

    authed.delete("/api/swipes/:nameId", async (req) => {
      const { nameId } = req.params as { nameId: string };
      deleteSwipe(db, req.device, nameId);
      notify(req.device);
      return { ok: true };
    });

    authed.post("/api/names", async (req) => {
      const body = z
        .object({
          id: z.string().min(1).max(100),
          name: z.string().trim().min(1).max(60),
          gender: Gender,
          origin: z.string().max(80).default(""),
          meaning: z.string().max(300).default(""),
          pronunciation: z.string().max(80).default(""),
          styleTags: z.array(z.string().max(40)).max(10).default([]),
        })
        .parse(req.body);
      const saved = addCustomName(db, {
        ...body,
        popularityRank: 999,
        addedBy: req.device.id,
        source: "user",
        createdAt: Date.now(),
      });
      notify(req.device);
      return saved;
    });

    authed.patch("/api/matches/:nameId", async (req, reply) => {
      const { nameId } = req.params as { nameId: string };
      const body = z
        .object({ rating: z.number().int().min(1).max(5).optional(), notes: z.string().max(1000).optional() })
        .parse(req.body);
      if (!req.device.coupleId || !updateMatch(db, req.device.coupleId, nameId, body)) {
        return reply.code(404).send({ error: "match_not_found" });
      }
      notify(req.device);
      return { ok: true };
    });

    authed.delete("/api/matches/:nameId", async (req, reply) => {
      const { nameId } = req.params as { nameId: string };
      if (!req.device.coupleId || !updateMatch(db, req.device.coupleId, nameId, { dismissed: true })) {
        return reply.code(404).send({ error: "match_not_found" });
      }
      notify(req.device);
      return { ok: true };
    });

    // --- AI ---

    const saveIdeas = (device: Device, ideas: NameIdea[], avoid: string[]): CustomName[] => {
      const seen = new Set(avoid.map((a) => a.toLowerCase()));
      const saved: CustomName[] = [];
      for (const idea of ideas) {
        const key = idea.name.trim().toLowerCase();
        if (!key || seen.has(key)) continue;
        seen.add(key);
        saved.push(
          addCustomName(db, {
            id: `ai_${randomUUID().slice(0, 12)}`,
            name: idea.name.trim(),
            gender: idea.gender,
            origin: idea.origin,
            meaning: idea.meaning,
            pronunciation: idea.pronunciation,
            styleTags: idea.styleTags.slice(0, 4),
            popularityRank: 999,
            addedBy: device.id,
            source: "ai",
            createdAt: Date.now(),
          }),
        );
      }
      if (saved.length) notify(device);
      return saved;
    };

    authed.post("/api/ai/suggest", async (req, reply) => {
      const ctx: IdeaContext = IdeaRequest.parse(req.body);
      if (!aiGuard(req, reply)) return;
      return { names: saveIdeas(req.device, await ai.suggest(ctx), ctx.avoid) };
    });

    authed.post("/api/ai/describe", async (req, reply) => {
      const body = IdeaRequest.extend({ prompt: z.string().trim().min(2).max(500) }).parse(req.body);
      if (!aiGuard(req, reply)) return;
      return { names: saveIdeas(req.device, await ai.describe(body.prompt, body), body.avoid) };
    });

    authed.post("/api/ai/taste", async (req, reply) => {
      const body = z
        .object({
          myLikes: z.array(NameBriefSchema).max(300),
          partnerLikes: z.array(NameBriefSchema).max(300),
          dislikes: z.array(NameBriefSchema).max(300),
        })
        .parse(req.body);
      if (!aiGuard(req, reply)) return;
      const state = getState(db, req.device);
      const summary = await ai.taste({
        myName: state.me.displayName,
        partnerName: state.partner?.displayName ?? null,
        ...body,
      });
      return { summary };
    });

    authed.post("/api/ai/deep-dive", async (req, reply) => {
      const body = z
        .object({ name: NameBriefSchema, lastName: z.string().trim().max(60).nullable().default(null) })
        .parse(req.body);
      if (!aiGuard(req, reply)) return;
      return ai.deepDive(body.name, body.lastName || null);
    });

    // Live change notifications. The room is fixed at connect time, so clients
    // reconnect after pairing or unpairing.
    authed.get("/api/ws", { websocket: true }, (socket, req) => {
      hub.join(roomOf(req.device), socket);
      socket.send(JSON.stringify({ type: "hello" }));
    });
  });

  return app;
}
