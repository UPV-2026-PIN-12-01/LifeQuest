---
name: PIN-612 S2 threads
overview: Save one conversation per thread and its messages. The server loads that thread into Groq. The chat page reloads it. No tools.
todos:
  - id: s2-threads
    content: Persist chat threads and messages, send the stored thread to Groq, and point the chat page at them. Then run the Test section.
    status: pending
isProject: false
---

# S2 — saved threads

Step 2 of [PIN-612](prompt.md). Step 3 (tools that create tasks, missions, challenges) is out. Depends on [S1](pin612_s1_chat.plan.md), which is already in the backend.

The page already calls `POST /api/chat/messages` and keeps threads in a tab variable. A reload starts over, and Groq still sees only the new text.

## Decisions

**High — ack these**

- [NEW] [HUMAN] A user has threads. Each thread has its messages, in order. No tools, no task or challenge writes.
- [NEW] [AI] The page uses the saved threads. A reload shows them again. A new chat stays only on the page until the first successful reply, then it gets the server id.
- [NEW] [AI] `POST /api/chat/messages` body is `{ "message", "threadId" }`. No `threadId` creates a thread. Response is `{ "reply", "threadId" }`. `reply` stays. The client still does not send prior messages.
- [NEW] [AI] On that post the server loads the stored messages of that thread and sends them to Groq before the new text. Another thread is not included.
- [NEW] [AI] The user message and the reply are written together, only after Groq returns. A 502 leaves the thread as it was. A missing or foreign `threadId` is 404 and does not call Groq.

**Low — keep unless you say otherwise**

- [NEW] [AI] `GET /api/chat/threads` returns `{ id, title, preview, updatedAt }` newest first. `GET /api/chat/threads/{id}` returns `{ id, title, messages: [{ role, text }] }`. 404 `{ "error": "Chat not found" }` when the id is missing or belongs to someone else.
- [NEW] [AI] Title is the first user message, trimmed, cut at 32 characters, and it does not change later. Preview is the last message, cut at 80. The greeting on a new chat is page copy and is not stored.
- [NEW] [AI] Only `user` and `assistant` are stored. Groq receives at most the last 20 stored messages plus the new one. Older rows stay in the thread and still show on the page.
- [NEW] [AI] Drop the system line that says there are no earlier messages. Keep the line that this version cannot create or change anything.
- [NEW] [AI] Tables `chat_threads` and `chat_messages` in a new supabase migration. RLS select-own, same idea as tasks. No write policy for the `authenticated` role. Backend keeps using the postgres role. No generated tests. Do not reset the database.

## Lands

- Migration `supabase/migrations/20261109200000_chat_threads.sql`. Apply it to the database in `DATABASE_URL` before testing (`supabase migration up --local` when that is local Supabase). `ddl-auto` is `none`, so a restart does not create the tables. If apply fails, stop.
- `chat_threads`: `id`, `user_id` → `users(id)` on delete cascade, `title`, `created_at`, `updated_at`. Index `(user_id, updated_at desc)`.
- `chat_messages`: `id`, `thread_id` → `chat_threads(id)` on delete cascade, `role` check `user` or `assistant`, `content`, `created_at`. Index `(thread_id, id)`.
- Entities `ChatThread` and `ChatMessage`, repositories, and `ChatService` writes. `GroqClient.reply` takes the prior turns. Blank and 2000-char rules stay.
- `frontend/src/services/chat.js` lists threads, loads one, and posts `{ message, threadId }` via `requestJson` (not the cached `getJson`). Remove the tab-only session.
- `GuidePage` loads the list on open. The unsaved "Chat nuevo" plus the greeting exist only before the first success. After that, the bubble list is the stored messages.

Does not land: delete, rename, tools, proposal cards, edits to tasks or challenges.

## Test

Implementer runs this after the migration is applied, the backend is on `http://localhost:8080`, and the frontend is on `http://localhost:5173`, with `GROQ_API_KEY` set. If the key or the tables are missing, stop. Do not fake a reply.

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$password = "secret1"
$word = "test-can-delete-later-word-$stamp"
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

# 401
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Content-Type: application/json" --data-binary '{"message":"hola"}'

# 400
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary '{"message":"   "}'

$first = @{ message = "$word es mi palabra. Responde solo OK." } | ConvertTo-Json -Compress
# 200 — creates a thread
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $first

# Use the threadId from that body as $threadId, then:
$follow = @{ message = "Copia tal cual mi palabra secreta y nada mas."; threadId = $threadId } | ConvertTo-Json -Compress
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $follow

$other = @{ message = "Copia tal cual mi palabra secreta. Si no la tienes, di NO." } | ConvertTo-Json -Compress
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $other

curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:8080/api/chat/threads" -H "Authorization: Bearer $tokenA"
curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:8080/api/chat/threads" -H "Authorization: Bearer $tokenB"
curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:8080/api/chat/threads/$threadId" -H "Authorization: Bearer $tokenA"
curl.exe -s -w "`nHTTP:%{http_code}" "http://localhost:8080/api/chat/threads/$threadId" -H "Authorization: Bearer $tokenB"
```

Expect: 401 without a token. 400 `{ "error": "message is required" }` for blank. First 200 has `reply` and a `threadId`. The follow-up on that id contains `$word`. The post with no `threadId` does not contain `$word`. User A has both threads; user B's list is empty. User B gets 404 `{ "error": "Chat not found" }` on A's thread. A's thread body has the user text and both replies, in order. Title is the first 32 characters of the first message.

Then on the chat page, logged in as user A: the saved thread is in the list after a reload, and its messages are the stored ones. A new chat does not know `$word`.
