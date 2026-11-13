---
name: PIN-612 S1 chat test
overview: Tester only. Check that a logged-in user gets a Groq reply that knows their own data and nobody else's, and that the chat changes nothing.
todos:
  - id: s1-chat-test
    content: Run the product checks against the running backend. Do not change code.
    status: pending
isProject: false
---

# S1 chat — tester

You test the product. You do not implement, refactor, or read for style. Backend is already on `http://localhost:8080`.

If `GROQ_API_KEY` is missing or the chat call returns 502, stop and say the live reply checks could not run. Do not invent a reply and do not mark those checks passed.

## Product

- A logged-in user sends one text and gets one text reply.
- The reply may use that user's username, this week's tasks (Monday–Sunday, Europe/Madrid), today's missions, and active challenges.
- Another user's tasks and challenges never appear in the reply.
- This version does not remember earlier messages. Each call only sees the new text and the live app data.
- Empty text is rejected. No token is rejected.
- Sending a message does not create, edit, or delete tasks, missions, or challenges.
- Chats are not saved.

Out of scope: the chat page, saved threads, the assistant creating things.

## Tests

Usernames and task titles must contain `test-can-delete-later`.

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$password = "secret1"
function Signup($name) {
  curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/auth/signup" `
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

$from = $day.AddDays(-6).ToString("yyyy-MM-dd")
$tasksBefore = curl.exe -s "http://localhost:8080/api/players/me/tasks?from=$from&to=$date" -H "Authorization: Bearer $tokenA"
$chalsBefore = curl.exe -s "http://localhost:8080/api/players/me/challenges" -H "Authorization: Bearer $tokenA"

# 401
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Content-Type: application/json" --data-binary '{"message":"hola"}'

# 400
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary '{"message":"   "}'

$ask = @{ message = "Responde solo con dos líneas. Línea 1: mi username. Línea 2: copia tal cual el título de mi tarea de esta semana y el nombre de mi desafío activo. No añadas nada más." } | ConvertTo-Json -Compress
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $ask

$ask2 = @{ message = "¿Qué te acabo de pedir en el mensaje anterior? Si no tienes ese mensaje, di NO." } | ConvertTo-Json -Compress
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $ask2

$tasksAfter = curl.exe -s "http://localhost:8080/api/players/me/tasks?from=$from&to=$date" -H "Authorization: Bearer $tokenA"
$chalsAfter = curl.exe -s "http://localhost:8080/api/players/me/challenges" -H "Authorization: Bearer $tokenA"
```

Pass when all of these hold:

- No token → 401.
- Blank message → 400 and `{ "error": "message is required" }`.
- First reply is 200, field `reply` contains the username `test-can-delete-later-a-<stamp>`, the task title, and the challenge name.
- That reply does not contain user B's task title. One retry is allowed if the model paraphrases A's own title. B's title at any time is a fail.
- Second reply says it does not have the previous message.
- The task list and the challenge list are the same before and after the two chat calls.
