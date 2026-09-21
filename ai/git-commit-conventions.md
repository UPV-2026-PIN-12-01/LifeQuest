# Git Commit Conventions

This is the format I usually follow when writing commits.

Format -> `PIN-{Worki_ID} type(scope): short summary`

- **Worki_ID** — the ticket number for that change
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

Summary of https://www.conventionalcommits.org/
