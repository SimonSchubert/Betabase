#!/usr/bin/env node
// Fetches the latest uploads for every athlete YouTube channel and writes a
// single combined JSON the app can fetch in one request, instead of having each
// device hit ~25 Atom feeds in parallel (which YouTube rate-limits per IP).
//
// No API key — uses the same public Atom feed the app used to call directly
// (https://www.youtube.com/feeds/videos.xml?channel_id=UC…). The XML is parsed
// by hand with the same rules as YoutubeChannelSource.kt (skip Shorts; keep
// videoId/title/published) so the output matches what the app expects.
//
// Resilient by design: a channel that fails this run keeps its previous entries
// (merged from the existing file) rather than vanishing until the next success.

import { readFile, writeFile, mkdir } from "node:fs/promises";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = join(dirname(fileURLToPath(import.meta.url)), "..");
const RES_DIR = "composeApp/src/commonMain/composeResources/files";
const ATHLETE_FILES = [
  "ifsc_athletes_overrides.json",
  "ifsc_athletes.json",
  "ifsc_athletes_retired.json",
];
const OUTPUT = join(ROOT, "data", "athlete_videos.json");

const FEED_BASE = "https://www.youtube.com/feeds/videos.xml?channel_id=";
const MAX_PER_CHANNEL = 15;
const REQUEST_DELAY_MS = 500; // be gentle; sequential anyway
const REQUEST_TIMEOUT_MS = 15000;
const CHANNEL_ID = /^UC[\w-]{22}$/;

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

/** Collect every distinct UC… channel id referenced by the athlete data. */
async function collectChannelIds() {
  const ids = new Set();
  for (const name of ATHLETE_FILES) {
    let parsed;
    try {
      parsed = JSON.parse(await readFile(join(ROOT, RES_DIR, name), "utf8"));
    } catch (err) {
      console.warn(`! could not read ${name}: ${err.message}`);
      continue;
    }
    const records = [...(parsed.overrides ?? []), ...(parsed.athletes ?? [])];
    for (const r of records) {
      const id = r?.youtube_channel_id;
      if (typeof id === "string" && CHANNEL_ID.test(id)) ids.add(id);
    }
  }
  return [...ids].sort();
}

// --- Atom parsing (mirrors YoutubeChannelSource.kt) ---

const ENTRY = /<entry\b[^>]*>([\s\S]*?)<\/entry>/g;
const SHORT_LINK = /<link\b[^>]*href="[^"]*\/shorts\//;
const VIDEO_ID = /<(?:yt:)?videoId>([^<]+)<\/(?:yt:)?videoId>/;
const WATCH_LINK = /watch\?v=([\w-]+)/;
const TITLE = /<title\b[^>]*>([\s\S]*?)<\/title>/;
const PUBLISHED = /<published>([^<]+)<\/published>/;

function unescapeXml(value) {
  return value
    .replaceAll("&lt;", "<")
    .replaceAll("&gt;", ">")
    .replaceAll("&quot;", '"')
    .replaceAll("&#39;", "'")
    .replaceAll("&apos;", "'")
    .replaceAll("&amp;", "&");
}

function parseEntries(xml) {
  const videos = [];
  for (const match of xml.matchAll(ENTRY)) {
    const entry = match[1];
    if (SHORT_LINK.test(entry)) continue; // drop Shorts
    const id = VIDEO_ID.exec(entry)?.[1] ?? WATCH_LINK.exec(entry)?.[1];
    if (!id) continue;
    const published = PUBLISHED.exec(entry)?.[1];
    if (!published || Number.isNaN(Date.parse(published))) continue;
    const title = unescapeXml((TITLE.exec(entry)?.[1] ?? "").trim());
    videos.push({ id, title, published_at: published });
  }
  return videos
    .sort((a, b) => Date.parse(b.published_at) - Date.parse(a.published_at))
    .slice(0, MAX_PER_CHANNEL);
}

async function fetchChannel(channelId) {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);
  try {
    const res = await fetch(FEED_BASE + channelId, {
      headers: {
        Accept: "application/atom+xml, text/xml, */*",
        "User-Agent": "Betabase/0.1 (+https://github.com/SimonSchubert/Betabase)",
      },
      signal: controller.signal,
    });
    if (!res.ok) {
      const err = new Error(`HTTP ${res.status}`);
      err.status = res.status; // let the caller single out 429s
      throw err;
    }
    return parseEntries(await res.text());
  } finally {
    clearTimeout(timer);
  }
}

async function readExisting() {
  try {
    return JSON.parse(await readFile(OUTPUT, "utf8")).channels ?? {};
  } catch {
    return {};
  }
}

async function main() {
  const channelIds = await collectChannelIds();
  if (channelIds.length === 0) {
    console.error("No channel ids found — refusing to write an empty file.");
    process.exit(1);
  }
  console.log(`Fetching ${channelIds.length} channels…`);

  const previous = await readExisting();
  const channels = {};
  let ok = 0;
  let failed = 0;
  let rateLimited = 0;

  for (const id of channelIds) {
    try {
      const videos = await fetchChannel(id);
      channels[id] = videos;
      ok++;
      console.log(`  ✓ ${id} (${videos.length})`);
    } catch (err) {
      failed++;
      if (err.status === 429) rateLimited++;
      // Keep the previous snapshot so a transient failure doesn't drop content.
      if (previous[id]) channels[id] = previous[id];
      console.warn(`  ✗ ${id}: ${err.message}${previous[id] ? " (kept previous)" : ""}`);
    }
    await sleep(REQUEST_DELAY_MS);
  }

  // Surface 429s as a CI annotation so we react to real data, not guesses,
  // before deciding to back off the delay or shard the refresh across runs.
  if (rateLimited > 0) {
    console.log(
      `::warning title=YouTube rate limiting::${rateLimited}/${channelIds.length} channel(s) ` +
        `returned HTTP 429 — consider raising REQUEST_DELAY_MS or sharding across runs.`,
    );
  }

  if (ok === 0) {
    console.error("Every channel failed — refusing to overwrite with stale-only data.");
    process.exit(1);
  }

  // Sort keys for stable, minimal diffs (no timestamp field, so unchanged data
  // produces an identical file and therefore no commit).
  const sorted = {};
  for (const id of Object.keys(channels).sort()) sorted[id] = channels[id];

  await mkdir(dirname(OUTPUT), { recursive: true });
  await writeFile(OUTPUT, JSON.stringify({ channels: sorted }, null, 2) + "\n");
  console.log(
    `Wrote ${OUTPUT} — ${ok} ok, ${failed} failed` +
      `${rateLimited ? `, ${rateLimited} rate-limited` : ""}.`,
  );
}

await main();
