---
name: PIN-612 S3 tools
overview: The counselor proposes a task (a misión) or a challenge. Nothing is created until the user presses Crear. The daily card stays computed.
todos:
  - id: s3-tools
    content: Add propose tools, store pending proposals, and confirm or discard them from the chat page. Then run the Test section.
    status: pending
isProject: false
---

# S3 — propose, then confirm

Step 3 of [PIN-612](prompt.md). Depends on [S2](pin612_s2_threads.plan.md), which is already in the app. The chat page reloads the thread after each reply. Proposal styles already exist in `guide.css` and are unused.

## Decisions

**High — ack these**

- [NEW] [HUMAN] The counselor can create tasks and challenges for the logged-in user. It always asks permission first. It talks to the system with Groq functions.
- [NEW] [AI] Permission is the Crear button, not the model's manners. Groq only has propose functions. Those functions do not insert a task or a challenge. `POST /api/chat/proposals/{id}/confirm` does, through `TaskService.create` and `ChallengeService.create`. Descartar writes nothing.
- [NEW] [AI] A misión is a task. The counselor proposes a task or a desafío. A desafío sits on a task that user already has. The home card "Desafíos diarios" is computed and is not created.
- [NEW] [HUMAN] If the asked start already passed, the reply is still 200 and stores a pending Crear card whose start is later today or another day. An empty model reply is not a 502 for that ask.

**Low — keep unless you say otherwise**

- [NEW] [AI] One class per function, same idea as the context contributors. `propose_task` and `propose_challenge`. A new action later is a new class. No other writes.
- [NEW] [AI] `propose_task` needs title, date, start, end, and priority. Color, icon, and description default when omitted. `propose_challenge` needs name, difficulty, and a `taskId` from that user's own tasks. `why` is one sentence for the card. An invalid call returns the error to Groq and stores nothing.
- [NEW] [AI] A proposal row hangs off the assistant message: kind, title, when, why, the request JSON, status `pending`. Reload still shows the card. Confirm moves it to `created`. A second confirm is 409. Someone else's id is 404 and does not call Groq or create anything.
- [NEW] [AI] The tool loop stays inside the one `POST /api/chat/messages`. One message proposes at most 10 things, and the loop has enough rounds to finish that plus the limit sentence. Do not put the round cap back to 3. The user text, the reply, and the pending proposals are stored together only after Groq finishes. A 502 leaves the thread as it was. `reply` and `threadId` stay. Replace the system line that says this version cannot create anything.
- [NEW] [AI] The card uses the existing `.guide-proposal` styles. Crear and Descartar. A created card keeps the done style. No new chat POST fields. The page already reloads the thread.

## Lands

- Migration `supabase/migrations/20261109230000_chat_proposals.sql`. Apply it to the database in `DATABASE_URL` before testing. Do not reset the database. If apply fails, stop.
- `chat_proposals`: `id`, `thread_id`, `user_id`, `message_id`, `kind` (`task` or `challenge`), `title`, `when_label`, `why`, `payload`, `status`, `created_at`. RLS select-own. No write policy for `authenticated`.
- `GroqClient` sends the tools and runs the tool round trip. `ChatService` drops the "cannot create" line and tells the model that a proposal is not created yet.
- `GET /api/chat/threads/{id}` puts each proposal on its assistant line.
- `POST /api/chat/proposals/{id}/confirm` and `POST /api/chat/proposals/{id}/discard`.
- `GuidePage` renders the card and calls those two routes.

Does not land: editing or deleting through the counselor, creating the "Desafíos diarios" card, tools that skip the button.

## Test

Implementer runs this after the migration is applied, the backend is on `http://localhost:8080`, and the frontend is on `http://localhost:5173`, with `GROQ_API_KEY` set. If the key or the tables are missing, stop. Do not fake a proposal.

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$password = "secret1"
$title = "test-can-delete-later-task-$stamp"
$chal = "test-can-delete-later-chal-$stamp"
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

$day = (Get-Date).Date
while ($day.DayOfWeek -ne 'Sunday') { $day = $day.AddDays(1) }
$date = $day.ToString("yyyy-MM-dd")

$ask = @{ message = "Propón una tarea, no la crees. Título exacto: $title. Fecha $date, 18:00 a 19:00, prioridad high, tipo STUDY." } | ConvertTo-Json -Compress
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $ask

# $threadId from that body. The task list must NOT contain $title yet.
curl.exe -s "http://localhost:8080/api/players/me/tasks?from=$date&to=$date" -H "Authorization: Bearer $tokenA"
curl.exe -s "http://localhost:8080/api/chat/threads/$threadId" -H "Authorization: Bearer $tokenA"

# $proposalId is the pending task proposal. Then:
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/proposals/$proposalId/confirm" `
  -H "Authorization: Bearer $tokenB"
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/proposals/$proposalId/confirm" `
  -H "Authorization: Bearer $tokenA"
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/proposals/$proposalId/confirm" `
  -H "Authorization: Bearer $tokenA"
curl.exe -s "http://localhost:8080/api/players/me/tasks?from=$date&to=$date" -H "Authorization: Bearer $tokenA"
```

Expect: the first reply is 200 and does not create the task. The thread has one pending task proposal whose title is `$title`. User B's confirm is 404. User A's first confirm is 200 and the task list then contains `$title`. The second confirm is 409. One retry is allowed if the model does not propose the first time. A missing proposal after that retry is a fail.

Then ask, on that same thread, for a challenge named `$chal` on that new task. Before confirm, the challenge list does not contain `$chal`. After confirm, it does.

On the chat page as user A, the card shows Crear. After Crear, the card shows it was created, and a reload still shows that.

Write each chat body as ASCII JSON in a file and send it with `curl.exe --data-binary @file`. A 400 that says the JSON could not be parsed is a bad request body. Send it again from the file.

Then ask for one task today at 08:00, a time that has already passed, with a `test-can-delete-later` title. The reply is 200. The thread has a pending task card. Its start is later today or another day, not in the past. A 502, or no card, is a fail. One retry is allowed.
