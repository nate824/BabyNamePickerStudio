// One-off: fill in origin/meaning/pronunciation/tags for a list of names via the Message Batches API.
// Usage (inside the kindred-sync container): node enrich.mjs /data/enrich
import Anthropic from "@anthropic-ai/sdk";
import { readFileSync, writeFileSync, existsSync } from "node:fs";

const dir = process.argv[2];
const input = JSON.parse(readFileSync(`${dir}/girls_1500.json`, "utf8"));
const client = new Anthropic();
const CHUNK = 50;

const TAGS = ["Classic", "Modern", "Vintage", "Nature", "Celestial", "Royal", "Short & Sweet", "Spiritual",
  "Melodic", "Unique", "Timeless", "Strong", "Soft", "Literary", "Mythological", "Biblical", "Trendy", "Floral"];

const schema = {
  type: "object",
  additionalProperties: false,
  required: ["names"],
  properties: {
    names: {
      type: "array",
      items: {
        type: "object",
        additionalProperties: false,
        required: ["name", "origin", "meaning", "pronunciation", "styleTags"],
        properties: {
          name: { type: "string" },
          origin: { type: "string" },
          meaning: { type: "string" },
          pronunciation: { type: "string" },
          styleTags: { type: "array", items: { type: "string", enum: TAGS } },
        },
      },
    },
  },
};

const prompt = (names) => `These are girls' names for cards in a baby-name swipe app. For each name, give:
- origin: the language or culture it comes from, 1-3 words (e.g. "Hebrew", "Latin", "Irish Gaelic"). For modern American coinages or blends, use "American".
- meaning: under 70 characters, warm but accurate. For coinages, say what it blends or evokes (e.g. "Modern blend of Jay and Lynn") rather than inventing a meaning.
- pronunciation: simple respelling with the stressed syllable in capitals, e.g. "ah-DEL-ine".
- styleTags: 2-3 tags that fit.
Return every name exactly as spelled, in the same order.

${names.join("\n")}`;

let batchId = existsSync(`${dir}/batch_id`) ? readFileSync(`${dir}/batch_id`, "utf8").trim() : null;
if (!batchId) {
  const requests = [];
  for (let i = 0; i < input.length; i += CHUNK) {
    requests.push({
      custom_id: `chunk-${i / CHUNK}`,
      params: {
        model: "claude-opus-5",
        max_tokens: 16000,
        output_config: { effort: "low", format: { type: "json_schema", schema } },
        messages: [{ role: "user", content: prompt(input.slice(i, i + CHUNK).map((n) => n.name)) }],
      },
    });
  }
  const batch = await client.messages.batches.create({ requests });
  batchId = batch.id;
  writeFileSync(`${dir}/batch_id`, batchId);
  console.log(`created ${batchId} with ${requests.length} requests`);
}

let batch;
while (true) {
  batch = await client.messages.batches.retrieve(batchId);
  console.log(new Date().toISOString(), batch.processing_status, JSON.stringify(batch.request_counts));
  if (batch.processing_status === "ended") break;
  await new Promise((r) => setTimeout(r, 30_000));
}

const byName = new Map(input.map((n) => [n.name.toLowerCase(), n]));
const out = [];
const failed = [];
let inTok = 0, outTok = 0;
for await (const r of await client.messages.batches.results(batchId)) {
  if (r.result.type !== "succeeded") { failed.push(r.custom_id); continue; }
  const msg = r.result.message;
  inTok += msg.usage.input_tokens; outTok += msg.usage.output_tokens;
  if (msg.stop_reason !== "end_turn") { failed.push(`${r.custom_id}:${msg.stop_reason}`); continue; }
  const text = msg.content.find((b) => b.type === "text")?.text ?? "";
  for (const n of JSON.parse(text).names) {
    const src = byName.get(n.name.trim().toLowerCase());
    if (!src) continue;
    out.push({ ...n, name: src.name, rank: src.rank, unisex: src.unisex });
  }
}
writeFileSync(`${dir}/enriched.json`, JSON.stringify(out));
console.log(`done: ${out.length} names, failed chunks: ${JSON.stringify(failed)}, tokens in=${inTok} out=${outTok}`);
