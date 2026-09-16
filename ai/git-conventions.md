# Git Conventions (for commits)

This is the format I usually follow when writing commits.

Format -> `PIN-{Worki_ID} type(scope): short summary`

Current work ID: **42**. Commits look like `PIN-42 type(scope): short summary`.

- **type** — required (`feat`, `fix`, etc.)
- **scope** — optional, in parentheses. Says where it changed (`auth`, `api`, `ui`)
- **summary** — one line, in imperative mood, no period at the end

Examples:

```
PIN-41 feat: add login with Google
PIN-98 fix(api): handle empty user list
```

Most used types:

- `feat` (new feature)
- `fix` (bug fix)
- `refactor`
- `docs`
- `test`
- `chore` (config, maintenance tasks)

Use `PIN-42`, not `#42`. GitHub treats `#42` as issue 42. A repo autolink on prefix `PIN-` can point to `https://cliente.tuneupprocess.com/web/#/wum/<num>`.

Summary of https://www.conventionalcommits.org/
