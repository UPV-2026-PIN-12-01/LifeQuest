TITLE: sign up

ASSUMPTIONS. things app collects. 
- Use backend -> (POST /api/auth/signup)

NOW, lets do the frontend implementation stuff.

YOU MUST gather these things.
1- Foto de perfil — optional
2- Username, email, password (min 6) — required
3- Clase (Guerrero / Arquero / Mago) — required in UI, default Guerrero
4- Icono emoji — optional; class default if skipped

also you should validate password length and email format.
    passwrod check on both frontend and backend could create future inconsistencies if any of these two change... propose to me if there is any popular solution for this. also put as option "just ignore the issue"

mockups will be provided in chat, if not just ask.

also well expand the backend so it resolves playerName vs characterName
    - this will be and idependent iteration, and it will be the first one.

After 201. can you put some confeti popup or whatever. then in text say TODO remove once dashboard done. say that both in code as the popup with that confeti.
    - no idea if this is too crazy?

Validation drift: duplicate the two rules on back and frontend
    Named constants on both sides + a “keep in sync” comment

leave ProfileDto as it is...

language: code stays in english, UI is in spanish.

for class portraits. put 3 place hodlers. ill put the images later

LATER. for now we skip this and leave it for later
JWT on protected routes
GET /api/me
actual classes images.
do something with ProfileDto
remove the confeti popup once dashboard is done (outside PIN-600)

PROCESS... PLAN -> IMPLEMENT -> TEST -> REVIEW. 
follow .cursor\personal-instructions\development-plan.md