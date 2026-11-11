---
name: PIN-612 S2 threads test
overview: Tester only. A chat survives reload, the counselor remembers that thread only, and another user cannot see it.
todos:
  - id: s2-threads-test
    content: Run the product checks against the running app. Do not change code.
    status: pending
isProject: false
---

# S2 threads — tester

You test the product. You do not implement, refactor, or read for style. Backend is on `http://localhost:8080`. The chat page is on `http://localhost:5173`.

If `GROQ_API_KEY` is missing, the chat call returns 502, or the thread endpoints say the tables are missing, stop. Do not invent a reply and do not mark those checks passed.

## Product

- Each saved conversation is one thread with its messages, in order.
- The next message on that thread is answered with those messages in mind.
- A message on a new thread does not see another thread.
- A user does not see another user's threads. Asking for someone else's thread is rejected.
- A reload of the chat page still shows the saved threads and their messages.
- A brand new chat shows the greeting before the first successful reply, and that greeting is not a stored message.
- An empty message is rejected. No login is rejected.
- A failed reply does not add messages to the thread.
- Nothing here creates or changes tasks, missions, or challenges.

Out of scope: the counselor creating tasks, renaming a chat, deleting a chat.

## Tests

Usernames and the secret word must contain `test-can-delete-later`.

```powershell
$stamp = Get-Date -Format "yyyyMMddHHmmss"
$password = "secret1"
$word = "test-can-delete-later-word-$stamp"
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

curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Content-Type: application/json" --data-binary '{"message":"hola"}'

curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary '{"message":"   "}'

$first = @{ message = "$word es mi palabra. Responde solo OK." } | ConvertTo-Json -Compress
curl.exe -s -w "`nHTTP:%{http_code}" -X POST "http://localhost:8080/api/chat/messages" `
  -H "Authorization: Bearer $tokenA" -H "Content-Type: application/json" --data-binary $first

# $threadId is threadId from that response.
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

Pass when all of these hold:

- No token → 401.
- Blank message → 400 and `{ "error": "message is required" }`.
- The first reply is 200 and includes `reply` and `threadId`.
- The follow-up on that same thread contains `$word`. One retry is allowed if the model paraphrases around it. The word itself must show up.
- A new thread, the post with no `threadId`, does not contain `$word`.
- User A's list has the two threads. User B's list is empty.
- User B opening A's thread is 404 and `{ "error": "Chat not found" }`.
- A's thread contains the user texts and the replies in order. The title is the first 32 characters of the first message.

Then log in on the chat page as user A and reload:

- The thread with `$word` is still in the list.
- Opening it shows the stored messages, not only the greeting.
- A new chat does not know `$word`.

If the page cannot be opened, stop and say the reload check did not run.
