---
name: PIN-612 S1 chat
overview: One authenticated endpoint sends the user's text plus their live context to Groq and returns the reply. No saved chats, no tools, no frontend wiring.
todos:
  - id: s1-chat-endpoint
    content: "POST /api/chat/messages calls Groq with pluggable user context and returns { reply }. No DB, no tools, no frontend. Then run the Test section."
    status: pending
isProject: false
---

# S1 — Groq reply

Step 1 of [PIN-612](prompt.md). Steps 2 (save threads) and 3 (tools that create tasks, missions, challenges) are out.

The chat page stays mocked. It does not call this endpoint.

## Decisions

**High — ack these**

- [NEW] [HUMAN] One endpoint. It receives the user's text, attaches extra context, sends that to Groq, and returns Groq's text. No chat tables, no migrations, no tool calls, no writes to tasks, missions, or challenges.
- [NEW] [AI] `POST /api/chat/messages`. Body `{ "message": "..." }`. Response `{ "reply": "..." }`. The client does not send prior messages. Step 2 will load the thread on the server; `reply` stays.
- [NEW] [AI] Each context source is its own Spring bean (`ChatContextContributor`). The chat service only joins their text. A new source is a new class. It does not edit the Groq call.
- [NEW] [AI] Context is only the logged-in user, resolved like the other controllers (`UserService.getIdByAuthSubject`). Another user's tasks never go into the prompt.
- [NEW] [AI] Call Groq with `RestClient`, same idea as Supabase. No Groq SDK. Key is `GROQ_API_KEY` in the repo-root `.env` (already imported). Never commit the key, never log it, never put it in a response.

**Low — keep unless you say otherwise**

- [NEW] [AI] Sources now, in this order: profile (`PlayerService.getProfile`, skip `profileUrl`), this week's tasks (`TaskService.findForUser`, Monday–Sunday via the existing `Clock`, zone `Europe/Madrid`), today's missions (`PlayerService.getDailyQuests`; they are computed, not stored), active challenges (`ChallengeService.findForUser(userId, false)`).
- [NEW] [AI] Each source returns a short Spanish text block (names, dates, times, status, progress). An empty list is the word `ninguna`. If one source throws, that block is `no disponible` and the call still goes to Groq.
- [NEW] [AI] System prompt: Spanish counselor. Use only the attached context. Do not invent tasks or challenges. This version cannot create or change anything; if asked, say so. Do not send a `tools` field.
- [NEW] [AI] Blank message → 400 `message is required`. Over 2000 chars → 400 `message must be at most 2000 characters`. Both via `InvalidRequestException`. Missing or failed Groq (no key, timeout, bad body, empty reply) → 502 `{ "error": "Chat is unavailable" }`. Do not forward Groq's body. 401 is Spring Security.
- [NEW] [AI] Model property `lifequest.groq.model`, default `llama-3.3-70b-versatile`. URL property default `https://api.groq.com/openai/v1/chat/completions`. Timeout 20s. `max_tokens` 600. Temperature 0.3.
- [NEW] [AI] No generated tests. Do not change `OpenApiConfigTest`. Add empty `GROQ_API_KEY=` to `.env.example`.

## Lands

- `POST /api/chat/messages` on a new `ChatController`. JWT required. Not added to `permitAll`.
- `ChatService` builds the system text from `List<ChatContextContributor>` and calls `GroqClient`.
- Contributors: `ProfileChatContext`, `WeekTasksChatContext`, `DailyQuestsChatContext`, `ActiveChallengesChatContext`.
- `GroqProperties` + a `RestClient` bean. `ChatUnavailableException` mapped in `GlobalExceptionHandler`.
- `application.properties`: `lifequest.groq.api-key=${GROQ_API_KEY:}`, plus model and url.
- Example block the model sees:

```
Perfil: Ada, Guerrero, nivel 3, racha 4.
Tareas de esta semana (2026-11-02 a 2026-11-08):
- 2026-11-08 09:00–10:30 Repaso (pendiente)
Misiones de hoy:
- Completa 2 tareas hoy 0/2
Desafíos activos:
- ninguna
```

Does not land: threads, messages table, tool calls, frontend changes, edits to task or challenge behavior.

## Test

Implementer runs this after the backend is up on `http://localhost:8080` with `GROQ_API_KEY` set. If the key is missing, stop and say so. Do not fake a reply.

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$password = "secret1"
function Signup($name) {
  curl.exe -s -X POST "http://localhost:8080/api/auth/signup" `
    -H "Content-Type: application/x-www-form-urlencoded" `
    --data-urlencode "username=$name" `
    --data-urlencode "email=$name@lifequest.test" `
    --data-urlencode "password=$password" `
    --data-urlencode "playerClass=Guerrero" `
    --data-urlencode "userIcon=⚔️"
}
Signup "test-can-delete-later-a-$stamp"
Signup "test-can-delete-later-b-$stamp"
function Token($name) {
  $login = curl.exe -s -X POST "http://localhost:8080/api/auth/login" `
    -H "Content-Type: application/x-www-form-urlencoded" `
    --data-urlencode "email=$name@lifequest.test" `
    --data-urlencode "password=$password" | ConvertFrom-Json
  return $login.access_token
}
$tokenA = Token "test-can-delete-later-a-$stamp"
$tokenB = Token "test-can-delete-later-b-$stamp"

# Sunday of this week, Europe/Madrid, so the start is still in the future on a Saturday.
$day = (Get-Date).Date
while ($day.DayOfWeek -ne 'Sunday') { $day = $day.AddDays(1) }
$date = $day.ToString("yyyy-MM-dd")
$titleA = "test-can-delete-later-A-$stamp"
$titleB = "test-can-delete-later-B-$stamp"
$chal = "test-can-delete-later-chal-$stamp"
function TaskBody($title) {
  @{ title = $title; description = ""; icon = "📘"; color = "blue"; date = $date
     startTime = "18:00"; endTime = "19:00"; priority = "high"; activityType = "STUDY" } | ConvertTo-Json -Compress
}
$created = curl.exe -s -X POST "http://localhost:8080/api/players/me/tasks" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary (TaskBody $titleA) | ConvertFrom-Json
curl.exe -s -X POST "http://localhost:8080/api/players/me/tasks" `
  -H "Authorization: Bearer $tokenB" -H "Content-Type: application/json" --data-binary (TaskBody $titleB) | Out-Null
$chalBody = @{ name = $chal; description = ""; difficulty = "EASY"; taskId = $created.id; requirement = "" } | ConvertTo-Json -Compress
curl.exe -s -X POST "http://localhost:8080/api/players/me/challenges" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $chalBody | Out-Null

# 401
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Content-Type: application/json" --data-binary '{"message":"hola"}'

# 400 blank
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary '{"message":"   "}'

$ask = @{ message = "Responde solo con dos líneas. Línea 1: mi username. Línea 2: copia tal cual el título de mi tarea de esta semana y el nombre de mi desafío activo. No añadas nada más." } | ConvertTo-Json -Compress
# 200 — reply contains username, titleA and chal; does not contain titleB
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $ask

# 200 — second call does not know the first message
$ask2 = @{ message = "¿Qué te acabo de pedir en el mensaje anterior? Si no tienes ese mensaje, di NO." } | ConvertTo-Json -Compress
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $ask2
```

Expect: 401 with no token. 400 `{ "error": "message is required" }` for blank. First 200 `reply` contains `test-can-delete-later-a-<stamp>`, `titleA`, and `chal`, and does not contain `titleB`. Second 200 says it does not have the previous message (the word `NO` is enough). Task and challenge lists are unchanged by the chat calls. One retry is ok if the model paraphrases; `titleB` in the reply is always a fail.
