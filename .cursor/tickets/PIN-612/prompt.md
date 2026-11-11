frontend esta hecho.

nos falta esto del backend:
1. Un endpoint en el backend que envie los mensajes a groq y responda.
Recibe el texto del usuario. Y si es posible lo junta con contexto extra (de momento no esta claro que pondremos aqui, pero seguro que... perfil, tareas de la semana, misiones del día y desafíos activos, etc... haz que sea facil añadir mas... cuando haga falta).
2. Guardar los chats. Una conversación por hilo y sus mensajes
3. Que el agente sea capaz de crear tareas, missiones, desafios, etc, en vez del usuario. El agente siempre debe pedir permiso antes, y interactua con el sistema usando funciones de groq.

note that missiones y tareas is the same. also the agent can create desafios, but these need an existing tarea.