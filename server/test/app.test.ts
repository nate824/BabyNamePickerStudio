import type { FastifyInstance } from "fastify";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import WebSocket from "ws";
import { AiRefusedError, type AiService, type NameIdea } from "../src/ai.js";
import { buildApp } from "../src/app.js";
import { openDb } from "../src/db.js";

const idea = (name: string): NameIdea => ({
  name,
  gender: "GIRL",
  origin: "Latin",
  meaning: "test",
  pronunciation: name,
  styleTags: ["Classic"],
});

class FakeAi implements AiService {
  calls: string[] = [];
  refuse = false;
  async suggest() {
    this.calls.push("suggest");
    return [idea("Aurelia"), idea("Olivia"), idea("Aurelia")];
  }
  async describe(prompt: string) {
    this.calls.push(`describe:${prompt}`);
    if (this.refuse) throw new AiRefusedError("no");
    return [idea("Fern")];
  }
  async taste() {
    return "You two like vintage names.";
  }
  async deepDive() {
    return { nicknames: ["Rel"], lastNameFit: "Nice", famousNamesakes: [], middleNameIdeas: [], sibling: "", history: "" };
  }
}

let app: FastifyInstance;
let ai: FakeAi;

beforeEach(async () => {
  ai = new FakeAi();
  app = await buildApp({ db: openDb(":memory:"), ai, aiDailyLimit: 3 });
});
afterEach(() => app.close());

async function register(name: string): Promise<string> {
  const res = await app.inject({ method: "POST", url: "/api/devices", payload: { displayName: name } });
  expect(res.statusCode).toBe(200);
  return res.json().token;
}

function call(token: string, method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE", url: string, payload?: object) {
  return app.inject({ method, url, payload, headers: { authorization: `Bearer ${token}` } });
}

async function pair(): Promise<[string, string, string]> {
  const nate = await register("Nate");
  const wife = await register("Sarah");
  const code = (await call(nate, "POST", "/api/couples")).json().pairCode;
  const join = await call(wife, "POST", "/api/couples/join", { code: code.toLowerCase().replace("-", " ") });
  expect(join.statusCode).toBe(200);
  return [nate, wife, code];
}

const swipe = (token: string, nameId: string, liked: boolean, ts = Date.now()) =>
  call(token, "PUT", "/api/swipes", { swipes: [{ nameId, liked, ts }] });

describe("auth", () => {
  it("rejects requests without a valid token", async () => {
    expect((await app.inject({ method: "GET", url: "/api/state" })).statusCode).toBe(401);
    expect((await call("bogus", "GET", "/api/state")).statusCode).toBe(401);
  });
});

describe("pairing", () => {
  it("links two devices and shows each the other as partner", async () => {
    const [nate, wife, code] = await pair();
    expect(code).toMatch(/^[A-Z2-9]{3}-[A-Z2-9]{3}$/);
    const s1 = (await call(nate, "GET", "/api/state")).json();
    const s2 = (await call(wife, "GET", "/api/state")).json();
    expect(s1.partner.displayName).toBe("Sarah");
    expect(s2.partner.displayName).toBe("Nate");
    expect(s2.pairCode).toBe(code);
  });

  it("rejects unknown codes and a third member", async () => {
    const [, , code] = await pair();
    const third = await register("Intruder");
    expect((await call(third, "POST", "/api/couples/join", { code: "ZZZ-ZZZ" })).statusCode).toBe(404);
    expect((await call(third, "POST", "/api/couples/join", { code })).statusCode).toBe(409);
  });

  it("refuses to create a second couple while paired", async () => {
    const [nate] = await pair();
    expect((await call(nate, "POST", "/api/couples")).statusCode).toBe(409);
  });

  it("finds matches from swipes made before the partner joined", async () => {
    const nate = await register("Nate");
    const wife = await register("Sarah");
    await swipe(nate, "girl_olivia", true);
    await swipe(wife, "girl_olivia", true);
    const code = (await call(nate, "POST", "/api/couples")).json().pairCode;
    await call(wife, "POST", "/api/couples/join", { code });
    const state = (await call(nate, "GET", "/api/state")).json();
    expect(state.matches.map((m: { nameId: string }) => m.nameId)).toEqual(["girl_olivia"]);
  });
});

describe("swipes and matches", () => {
  it("creates a match when both like the same name and reports it once", async () => {
    const [nate, wife] = await pair();
    expect((await swipe(nate, "boy_liam", true)).json().newMatches).toEqual([]);
    expect((await swipe(wife, "boy_liam", true)).json().newMatches).toEqual(["boy_liam"]);
    expect((await swipe(wife, "boy_liam", true)).json().newMatches).toEqual([]);
    const state = (await call(nate, "GET", "/api/state")).json();
    expect(state.matches).toHaveLength(1);
    expect(state.swipes).toHaveLength(2);
  });

  it("does not match a like with a pass", async () => {
    const [nate, wife] = await pair();
    await swipe(nate, "boy_liam", true);
    expect((await swipe(wife, "boy_liam", false)).json().newMatches).toEqual([]);
  });

  it("removes the match when either partner undoes or flips their like", async () => {
    const [nate, wife] = await pair();
    await swipe(nate, "a", true);
    await swipe(wife, "a", true);
    await call(nate, "DELETE", "/api/swipes/a");
    expect((await call(wife, "GET", "/api/state")).json().matches).toHaveLength(0);

    await swipe(nate, "b", true);
    await swipe(wife, "b", true);
    await swipe(wife, "b", false);
    expect((await call(nate, "GET", "/api/state")).json().matches).toHaveLength(0);
  });

  it("ignores stale swipes (last write wins by timestamp)", async () => {
    const [nate] = await pair();
    await swipe(nate, "x", true, 2000);
    await swipe(nate, "x", false, 1000);
    const s = (await call(nate, "GET", "/api/state")).json();
    expect(s.swipes[0].liked).toBe(true);
  });

  it("shares rating and notes, and hides dismissed matches for both", async () => {
    const [nate, wife] = await pair();
    await swipe(nate, "a", true);
    await swipe(wife, "a", true);
    await call(nate, "PATCH", "/api/matches/a", { rating: 3, notes: "Grandma's name" });
    const m = (await call(wife, "GET", "/api/state")).json().matches[0];
    expect(m).toMatchObject({ nameId: "a", rating: 3, notes: "Grandma's name" });
    await call(wife, "DELETE", "/api/matches/a");
    expect((await call(nate, "GET", "/api/state")).json().matches).toHaveLength(0);
    // Re-swiping a dismissed match does not resurrect it
    expect((await swipe(nate, "a", true)).json().newMatches).toEqual([]);
  });

  it("stops sharing after a partner leaves", async () => {
    const [nate, wife] = await pair();
    await call(wife, "POST", "/api/couples/leave");
    const s = (await call(nate, "GET", "/api/state")).json();
    expect(s.partner).toBeNull();
  });
});

describe("custom names", () => {
  it("shares names added by either partner", async () => {
    const [nate, wife] = await pair();
    const res = await call(nate, "POST", "/api/names", {
      id: "custom_1",
      name: "Zephyr",
      gender: "BOY",
      origin: "Greek",
      meaning: "West wind",
      pronunciation: "ZEF-er",
      styleTags: ["Unique"],
    });
    expect(res.statusCode).toBe(200);
    const names = (await call(wife, "GET", "/api/state")).json().names;
    expect(names).toHaveLength(1);
    expect(names[0]).toMatchObject({ id: "custom_1", name: "Zephyr", source: "user" });
  });

  it("validates input", async () => {
    const [nate] = await pair();
    const res = await call(nate, "POST", "/api/names", { id: "x", name: "", gender: "CAT" });
    expect(res.statusCode).toBe(400);
  });
});

describe("AI", () => {
  it("saves suggested names to the shared deck, skipping duplicates and existing names", async () => {
    const [nate, wife] = await pair();
    const res = await call(nate, "POST", "/api/ai/suggest", { avoid: ["Olivia"], count: 5 });
    expect(res.statusCode).toBe(200);
    expect(res.json().names.map((n: { name: string }) => n.name)).toEqual(["Aurelia"]);
    const names = (await call(wife, "GET", "/api/state")).json().names;
    expect(names[0]).toMatchObject({ name: "Aurelia", source: "ai" });
  });

  it("passes the description through", async () => {
    const [nate] = await pair();
    const res = await call(nate, "POST", "/api/ai/describe", { prompt: "short irish names" });
    expect(res.json().names[0].name).toBe("Fern");
    expect(ai.calls).toContain("describe:short irish names");
  });

  it("maps refusals to 422", async () => {
    const [nate] = await pair();
    ai.refuse = true;
    expect((await call(nate, "POST", "/api/ai/describe", { prompt: "anything" })).statusCode).toBe(422);
  });

  it("enforces the daily quota per couple", async () => {
    const [nate, wife] = await pair();
    const body = { myLikes: [], partnerLikes: [], dislikes: [] };
    expect((await call(nate, "POST", "/api/ai/taste", body)).statusCode).toBe(200);
    expect((await call(wife, "POST", "/api/ai/taste", body)).statusCode).toBe(200);
    expect((await call(nate, "POST", "/api/ai/taste", body)).statusCode).toBe(200);
    const blocked = await call(wife, "POST", "/api/ai/taste", body);
    expect(blocked.statusCode).toBe(429);
    expect(blocked.json().error).toBe("ai_quota");
  });

  it("returns a deep dive", async () => {
    const [nate] = await pair();
    const res = await call(nate, "POST", "/api/ai/deep-dive", {
      name: { name: "Aurelia", gender: "GIRL" },
      lastName: "Smith",
    });
    expect(res.json().nicknames).toEqual(["Rel"]);
  });
});

describe("websocket", () => {
  it("pushes a change notification to the partner", async () => {
    const [nate, wife] = await pair();
    await app.listen({ port: 0, host: "127.0.0.1" });
    const port = (app.server.address() as { port: number }).port;
    const ws = new WebSocket(`ws://127.0.0.1:${port}/api/ws`, { headers: { authorization: `Bearer ${wife}` } });
    const messages: string[] = [];
    await new Promise<void>((resolve, reject) => {
      ws.on("message", (m) => {
        messages.push(m.toString());
        if (messages.length === 1) swipe(nate, "a", true).catch(reject);
        if (messages.length === 2) resolve();
      });
      ws.on("error", reject);
    });
    ws.close();
    expect(JSON.parse(messages[1])).toEqual({ type: "changed" });
  });

  it("rejects unauthenticated sockets", async () => {
    await app.listen({ port: 0, host: "127.0.0.1" });
    const port = (app.server.address() as { port: number }).port;
    const ws = new WebSocket(`ws://127.0.0.1:${port}/api/ws`);
    const status = await new Promise<number>((resolve) => ws.on("unexpected-response", (_r, res) => resolve(res.statusCode!)));
    expect(status).toBe(401);
  });
});
