TITLE: sign up

ASSUMPTIONS. things app collects. 
1- Foto de perfil — optional
2- Username, email, password (min 6) — required
3- Clase (Guerrero / Arquero / Mago) — required in UI, default Guerrero
4- Icono emoji — optional; class default if skipped

Backend ideas -> (POST /api/auth/signup)
Validate input. 
- this means 400 / 409 if bad or duplicate email/username (also if bad password). front will also validate email/password, but well do it too
- Create Auth user in supabase. Password stays in auth.users. Never in regular user table.
    - use admin create pattern. we just create user, dont wait them to verify email to become "trusted"
- Insert into the user table on public.users: username, email, user_icon if any, inner_id = Auth uuid.
    - if this one fails, we also delete the previous created admin user.
- If there is a photo: upload bytes to a Storage bucket (create avatars; none exists yet) → save the URL in profile_url. Skip photo → profile_url null, default avatar in UI.
- Return 201 + profile DTO (no password).
    - profile dto is just username, put a TODO comment to follow this up when doing frontned.
- playerName = username.

LATER. for now we skip this and leave it for later
JWT on protected routes
GET /api/me
entire fronend implementation side

PROCESS... PLAN -> IMPLEMENT -> TEST -> REVIEW. 
follow .cursor\personal-instructions\development-plan.md