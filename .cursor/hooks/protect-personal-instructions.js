"use strict";

const fs = require("fs");

const PROTECTED_RE = /\.cursor[/\\]+personal-instructions(?:[/\\]|$)/i;
const SKIP_KEYS = /^(contents|old_string|new_string|prompt|query|new_contents)$/i;
const READ_TOOLS = new Set(["Read", "Grep", "Glob", "SemanticSearch", "ReadFile", "TabRead"]);

const denyPayload = {
  permission: "deny",
  user_message: "Blocked: .cursor/personal-instructions/ is read-only.",
  agent_message:
    "A project hook denied create/edit/move/delete under .cursor/personal-instructions/, including when the user asked. Read those files; do not change them.",
};

function write(obj) {
  process.stdout.write(JSON.stringify(obj));
}

function mentionsProtected(text) {
  return Boolean(text) && PROTECTED_RE.test(String(text).replace(/\\/g, "/"));
}

function collect(value, acc, key) {
  if (typeof value === "string") {
    if (!SKIP_KEYS.test(key || "")) acc.push(value);
    return;
  }
  if (Array.isArray(value)) {
    for (const item of value) collect(item, acc, key);
    return;
  }
  if (value && typeof value === "object") {
    for (const [k, v] of Object.entries(value)) collect(v, acc, k);
  }
}

let input = {};
try {
  input = JSON.parse(fs.readFileSync(0, "utf8") || "{}");
} catch {
  write({ permission: "allow" });
  process.exit(0);
}

if (READ_TOOLS.has(input.tool_name || "")) {
  write({ permission: "allow" });
  process.exit(0);
}

const strings = [];
if (typeof input.command === "string") strings.push(input.command);
collect(input.tool_input, strings, "tool_input");

if (strings.some(mentionsProtected)) {
  write(denyPayload);
  process.exit(0);
}

write({ permission: "allow" });
