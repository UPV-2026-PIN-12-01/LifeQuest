---
name: PIN-612 S3 tools test
overview: Tester only. The counselor can propose a task or a challenge, and nothing exists until the user confirms.
todos:
  - id: s3-tools-test
    content: Run the product checks against the running app. Do not change code.
    status: pending
isProject: false
---

# S3 tools — tester

You test the product. You do not implement, refactor, or read for style. Backend is on `http://localhost:8080`. The chat page is on `http://localhost:5173`.

If `GROQ_API_KEY` is missing, the chat call returns 502, or the proposal routes say the table is missing, stop. Do not invent a proposal and do not mark those checks passed.

## Product

- The counselor can propose a task (a misión) and a desafío for the logged-in user. A desafío sits on a task that user already has.
- A proposal does not create the task or the challenge.
- Crear is what creates it. Descartar creates nothing.
- Another user cannot confirm or discard it.
- Confirming twice does not create a second copy.
- A reload still shows the card and whether it was created or discarded.
- The home card "Desafíos diarios" is not created by the counselor.
- A normal message that does not ask for anything new creates nothing.
- [NEW] The counselor text and the proposal card have a visible gap. The card is not stuck to the bubble.
- [NEW] A new challenge reads Activo. No superado is only after it was marked not passed. Desafío superado is only after it was passed. A future task on that card is not Fallada.
- [NEW] After Crear, the mission is on Inicio for that date. The created card says Creada and links Ver misiones to Inicio. A created challenge links Ver desafíos to the challenges page.
- [NEW] If the asked start already passed, the reply still has a Crear card, and the start is later today or another day. The text does not say to press Crear unless that card is there. It does not say a challenge was prepared unless a challenge card is there.
- [NEW] One message can propose several things. Two missions and two challenges come back as 200 with four pending cards, not the red "El consejero no está disponible ahora".
- [NEW] One message proposes at most 10 things, missions and challenges together. The sentence about the maximum is only used when 10 cards were stored. If Groq stops earlier, on a 429 or because the rounds run out, the cards already stored stay pending, and the reply says how many were left and asks for the rest in another message. It does not show the red unavailable message.
- [NEW] On a phone-width screen, Desafíos is in the bottom bar and opens the challenges page.

Out of scope: editing or deleting through the counselor, creating the "Desafíos diarios" card.

## Tests

Names must contain `test-can-delete-later`.

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$password = "secret1"
$title = "test-can-delete-later-task-$stamp"
$chal = "test-can-delete-later-chal-$stamp"
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

$ask = @{ message = "Propón una tarea, no la crees. Título exacto: $title. Fecha $date, 18:00 a 19:00, prioridad high, tipo STUDY." } | ConvertTo-Json -Compress
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $ask

curl.exe -s "http://localhost:8080/api/players/me/tasks?from=$date&to=$date" -H "Authorization: Bearer $tokenA"
curl.exe -s "http://localhost:8080/api/chat/threads/$threadId" -H "Authorization: Bearer $tokenA"

curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/proposals/$proposalId/confirm" `
  -H "Authorization: Bearer $tokenB"
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/proposals/$proposalId/confirm" `
  -H "Authorization: Bearer $tokenA"
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/proposals/$proposalId/confirm" `
  -H "Authorization: Bearer $tokenA"
curl.exe -s "http://localhost:8080/api/players/me/tasks?from=$date&to=$date" -H "Authorization: Bearer $tokenA"
```

`$threadId` is `threadId` from the chat response. `$proposalId` is the pending proposal on that thread.

Pass when all of these hold:

- The chat reply is 200.
- Before confirm, the task list does not contain `$title`.
- The thread shows one pending task proposal titled `$title`.
- User B's confirm is 404 and the task still does not exist.
- User A's confirm is 200 and the task list then contains `$title` once.
- Confirming that same proposal again is 409 and does not add a second task.
- One retry of the chat message is allowed if the first reply has no proposal. No proposal after the retry is a fail.

Then, on that same thread, ask for a challenge named `$chal` on that task. Before its confirm, the challenge list does not contain `$chal`. After confirm, it does, once.

Ask for one more task and discard it. The task list does not gain that title.

On the chat page as user A, reload and open the thread. The created card still shows that it was created. The discarded one does not offer Crear.

If the page cannot be opened, stop and say that check did not run.

Write each chat body as ASCII JSON in a file and send it with `curl.exe --data-binary @file`. A 400 that says the JSON could not be parsed is a bad request body, not a product fail. Send it again from the file.

## Extra checks

Same two users. Titles still contain `test-can-delete-later`. One retry of a chat message is allowed if the first reply has no proposal. No proposal after the retry is a fail. A 502, or the red "El consejero no está disponible ahora", is a fail on every check below.

- [NEW] Ask for one task today at 08:00, a time that has already passed. The thread has a pending task card with Crear. Its start is later today or another day, not in the past. The reply does not tell them to press Crear if that card is missing.
- [NEW] Ask for two missions and two challenges in one message, for a future hour. The chat reply is 200. The thread has four pending cards, two missions and two challenges. The task list and the challenge list stay unchanged until Crear.
- [NEW] Ask for eleven missions in one message, for a future hour, each with its own title. The reply is 200. The thread gains at most 10 pending cards from that message. It does not answer with the red unavailable message. If 10 cards were stored, the reply may say the maximum is 10. If fewer were stored and the reply says they were left pending, that count matches the cards and it asks for the rest in another message.
- [NEW] On the chat page as user A, the first proposal card is not flush with the counselor bubble. There is a visible gap.
- [NEW] Confirm the mission from the first check. On Inicio, that title is listed for its date. The card says Creada and Ver misiones. Ver misiones opens Inicio.
- [NEW] Confirm one challenge that sits on a future task. On the challenges page its badge is Activo, not No superado, and the task line is not Fallada. The card says Creada and Ver desafíos. Ver desafíos opens the challenges page.
- [NEW] At a phone width, the bottom bar shows Desafíos and that link opens the challenges page.
