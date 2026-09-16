# Git Conventions (for commits)

This is the format I usually follow when writing commits.

Format -> `#Worki_ID type(scope): short summary`

- **type** — required (`feat`, `fix`, etc.)
- **scope** — optional, in parentheses. Says where it changed (`auth`, `api`, `ui`)
- **summary** — one line, in imperative mood, no period at the end

Examples:

```
#41 feat: add login with Google
#98 fix(api): handle empty user list
```

Most used types:

- `feat` (new feature)
- `fix` (bug fix)
- `refactor`
- `docs`
- `test`
- `chore` (config, maintenance tasks)

Summary of https://www.conventionalcommits.org/
