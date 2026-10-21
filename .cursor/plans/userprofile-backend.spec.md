DONE already
- frontend

PLAN. my decisions.
new endpoint
GET /api/users/me
which returns
  "user": {
    "id": 1,
    "username": "Ada",
    "email": "ada@lifequest.test",
    "userIcon": "🦊"
  }
(also includes profileUrl for later, but we'll leave it for later)

also create new endpoints for updating the fields.
btw when there is blank password, we keep the current one.

also when modifying unique fields, backend should reject if they already exist. return 409.

when updating email...
users.email... we'll be using this to read the email, do not use /api/auth/me
so when we update email, we'll update users.email and Supabase Auth table. we'll leave the token as it is, let it refresh...

FOR LATER
- profileUrl