import Anthropic from "@anthropic-ai/sdk";
import { betaZodOutputFormat } from "@anthropic-ai/sdk/helpers/beta/zod";
import { z } from "zod";

/** Compact description of a name the couple has seen; sent up by the phone. */
export const NameBriefSchema = z.object({
  name: z.string().max(60),
  gender: z.enum(["BOY", "GIRL", "UNISEX"]),
  origin: z.string().max(80).optional().default(""),
  meaning: z.string().max(300).optional().default(""),
  styleTags: z.array(z.string().max(40)).max(10).optional().default([]),
});
export type NameBrief = z.infer<typeof NameBriefSchema>;

export const NameIdeaSchema = z.object({
  name: z.string(),
  gender: z.enum(["BOY", "GIRL", "UNISEX"]),
  origin: z.string(),
  meaning: z.string(),
  pronunciation: z.string(),
  styleTags: z.array(z.string()),
});
export type NameIdea = z.infer<typeof NameIdeaSchema>;

const IdeasSchema = z.object({ names: z.array(NameIdeaSchema) });
const TasteSchema = z.object({ summary: z.string() });
export const DeepDiveSchema = z.object({
  nicknames: z.array(z.string()),
  lastNameFit: z.string(),
  famousNamesakes: z.array(z.string()),
  middleNameIdeas: z.array(z.string()),
  sibling: z.string(),
  history: z.string(),
});
export type DeepDive = z.infer<typeof DeepDiveSchema>;

export interface TasteContext {
  myName: string;
  partnerName: string | null;
  myLikes: NameBrief[];
  partnerLikes: NameBrief[];
  dislikes: NameBrief[];
}

export interface IdeaContext {
  liked: NameBrief[];
  disliked: NameBrief[];
  /** Names already in the deck, so Claude doesn't repeat them. */
  avoid: string[];
  gender: "BOY" | "GIRL" | "UNISEX" | null;
  count: number;
}

export interface AiService {
  suggest(ctx: IdeaContext): Promise<NameIdea[]>;
  describe(prompt: string, ctx: IdeaContext): Promise<NameIdea[]>;
  taste(ctx: TasteContext): Promise<string>;
  deepDive(name: NameBrief, lastName: string | null): Promise<DeepDive>;
}

export class AiUnavailableError extends Error {}
export class AiRefusedError extends Error {}

const SYSTEM = `You help an expecting couple choose a baby name inside a swipe-style app.
Names you propose are added straight into the couple's deck as cards, so every field must be accurate and
self-contained: real names with correct origins, concise meanings (under 90 characters), and a simple
capitalised-stress pronunciation guide like "oh-LIV-ee-uh". Style tags are 2-3 short labels drawn from:
Classic, Modern, Vintage, Nature, Celestial, Royal, Short & Sweet, Spiritual, Melodic, Unique, Timeless,
Strong, Soft, Literary, Mythological. Never propose a name from the avoid list.`;

function briefLine(n: NameBrief): string {
  const tags = n.styleTags.length ? ` [${n.styleTags.join(", ")}]` : "";
  return `${n.name} (${n.gender.toLowerCase()}, ${n.origin || "unknown origin"})${tags}`;
}

function ideaPrompt(ctx: IdeaContext, request: string): string {
  const gender = ctx.gender ? `Only ${ctx.gender.toLowerCase()} names (unisex also fine).` : "Any gender.";
  return [
    request,
    gender,
    `Return exactly ${ctx.count} names.`,
    ctx.liked.length ? `Names they liked:\n${ctx.liked.map(briefLine).join("\n")}` : "They haven't liked any names yet.",
    ctx.disliked.length ? `Names they passed on:\n${ctx.disliked.map(briefLine).join("\n")}` : "",
    `Avoid list (already in their deck): ${ctx.avoid.join(", ")}`,
  ]
    .filter(Boolean)
    .join("\n\n");
}

export class ClaudeAiService implements AiService {
  private client: Anthropic;

  constructor(
    apiKey: string,
    private model: string,
    private effort: "low" | "medium" | "high",
  ) {
    this.client = new Anthropic({ apiKey });
  }

  private async ask<T>(schema: z.ZodType<T>, prompt: string, maxTokens = 16000): Promise<T> {
    const response = await this.client.beta.messages.parse({
      model: this.model,
      max_tokens: maxTokens,
      betas: ["server-side-fallback-2026-07-01"],
      fallbacks: "default",
      system: SYSTEM,
      output_config: { effort: this.effort, format: betaZodOutputFormat(schema) },
      messages: [{ role: "user", content: prompt }],
    });
    if (response.stop_reason === "refusal") throw new AiRefusedError("Claude declined this request");
    if (!response.parsed_output) throw new Error(`Unparseable Claude response (stop_reason=${response.stop_reason})`);
    return response.parsed_output as T;
  }

  async suggest(ctx: IdeaContext): Promise<NameIdea[]> {
    const out = await this.ask(
      IdeasSchema,
      ideaPrompt(
        ctx,
        "Suggest fresh names this couple is likely to love, based on the patterns in what they liked and passed on. Mix safe bets with a few pleasant surprises.",
      ),
    );
    return out.names;
  }

  async describe(prompt: string, ctx: IdeaContext): Promise<NameIdea[]> {
    const out = await this.ask(
      IdeasSchema,
      ideaPrompt(ctx, `The couple described what they want:\n<request>${prompt}</request>\nSuggest names that fit that description.`),
    );
    return out.names;
  }

  async taste(ctx: TasteContext): Promise<string> {
    const partner = ctx.partnerName ?? "their partner";
    const prompt = [
      `Write a warm, specific 3-5 sentence read on ${ctx.myName}'s naming taste, addressed to them ("you").`,
      `Describe the patterns in what they like and pass on, mention what the names they share with ${partner} have in common, and name one direction worth exploring.`,
      `${partner}'s other picks are private: only mention names from the lists below.`,
      `Plain text, no lists or markdown.`,
      `${ctx.myName} liked:\n${ctx.myLikes.map(briefLine).join("\n") || "(nothing yet)"}`,
      `Liked by both ${ctx.myName} and ${partner}:\n${ctx.partnerLikes.map(briefLine).join("\n") || "(nothing yet)"}`,
      `${ctx.myName} passed on:\n${ctx.dislikes.map(briefLine).join("\n") || "(nothing yet)"}`,
    ].join("\n\n");
    return (await this.ask(TasteSchema, prompt)).summary;
  }

  async deepDive(name: NameBrief, lastName: string | null): Promise<DeepDive> {
    const prompt = [
      `Give the couple a deep dive on the name ${briefLine(name)}${name.meaning ? ` meaning "${name.meaning}"` : ""}.`,
      `nicknames: 3-6 common nicknames. famousNamesakes: 3-5 notable people or characters, each with a few words of context.`,
      `middleNameIdeas: 4-6 middle names that flow well with it. sibling: one sentence suggesting sibling names in the same style.`,
      `history: 2-3 sentences on its history and how its popularity has changed.`,
      lastName
        ? `lastNameFit: 1-2 sentences on how "${name.name} ${lastName}" sounds together (rhythm, initials, anything awkward).`
        : `lastNameFit: 1-2 sentences on what kinds of last names it pairs well with.`,
    ].join("\n");
    return this.ask(DeepDiveSchema, prompt);
  }
}

/** Used when no ANTHROPIC_API_KEY is configured; every call reports the feature as unavailable. */
export class DisabledAiService implements AiService {
  private fail(): never {
    throw new AiUnavailableError("AI is not configured on the server (ANTHROPIC_API_KEY is missing)");
  }
  suggest(): Promise<NameIdea[]> {
    this.fail();
  }
  describe(): Promise<NameIdea[]> {
    this.fail();
  }
  taste(): Promise<string> {
    this.fail();
  }
  deepDive(): Promise<DeepDive> {
    this.fail();
  }
}
