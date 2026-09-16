---
name: create-skill
description: >-
  Create a project Cursor skill in .cursor/skills/. Use when the user asks to
  create, add, or write a skill, SKILL.md, or agent skill for this repo.
---

# Create a project skill

- Put it in `.cursor/skills/<skill-name>/SKILL.md` (shared with the repo). Never write to `~/.cursor/skills-cursor/`.
- Keep it ENOUGH. prefer undersrtandable one liners, with just enough information to be clearly understood
- Frontmatter: `name` (lowercase, hyphens) and `description` (third person: what it does and when to use it).
- Body: only what the agent would not already know; skip essays, options lists, and extra files unless needed.
- Do not copy `AGENTS.md` into the skill; that file is the shared source of truth.