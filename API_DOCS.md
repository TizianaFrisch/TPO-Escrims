# API de Scrims - Documentación de Endpoints

Base URL: http://localhost:8080

Este proyecto expone endpoints para gestionar scrims y usuarios. El modelo fue enriquecido con campos adicionales (tomados del otro proyecto, sin cambios de seguridad) y se reflejan en los ejemplos de respuesta. Los endpoints y flujos no cambian.

## Juegos

- GET /api/juegos

  - Body (ejemplo):
  ```json
  [
    { "id": 1, "nombre": "Valorant" },
    { "id": 2, "nombre": "League of Legends" }
  ]
  ```

- GET /api/juegos/{id}
  - Qué hace: Devuelve los datos de un juego por su identificador.
  - Ejemplo: /api/juegos/1
  - Respuesta 200
  - Body (ejemplo):
  ```json

## Auth

  - Qué hace: Crea un usuario nuevo y lo deja en estado de verificación PENDIENTE.
  - Body RegisterRequest
  ```json
  { "email": "alice@example.com", "password": "secret", "region": "LATAM", "username": "alice" }

  - Notas:
    - username es opcional: si no se envía, se deriva automáticamente de la parte local del email.

- POST /api/auth/login
  - Qué hace: Autentica a un usuario por email (simulado, sin JWT) y retorna información básica.
  - Body LoginByEmailRequest
  ```json
  { "email": "alice@example.com", "password": "secret" }
  - Respuesta 200
  ```json
  { "id": 1, "username": "alice", "verificacionEstado": "PENDIENTE" }
  ```

- POST /api/auth/verify/{id}
  - Qué hace: Marca al usuario como VERIFICADO (atajo para entorno de desarrollo).
  - Marca el usuario como VERIFICADO (simulado)
  - Respuesta 200: Usuario actualizado

## Usuarios

- GET /api/usuarios
  - Qué hace: Lista todos los usuarios registrados.
  - Respuesta 200
  ```json
  [
    {
      "id": 1,
      "username": "alice",
      "email": "alice@example.com",
      "region": "LATAM",
      "verificacionEstado": "PENDIENTE",
      "rol": "USUARIO",
      "mmr": 0,
      "rolPreferido": "DUELISTA",
      "discordId": null,
      "summoner": null,
      "activo": true,
      "fechaRegistro": "2025-10-17T18:55:00",
      "ultimaConexion": "2025-10-17T19:05:00"
    },
    { "id": 2, "username": "bob", "email": "bob@example.com", "region": "LATAM" }
  ]
  ```

- GET /api/usuarios/{id}
  - Qué hace: Devuelve el detalle de un usuario.
  - Respuesta 200
  ```json
  {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "region": "LATAM",
    "verificacionEstado": "VERIFICADO",
    "rol": "USUARIO",
    "mmr": 1200,
    "rolPreferido": "DUELISTA",
    "discordId": "alice#1234",
    "summoner": "AliceXYZ",
    "activo": true,
    "fechaRegistro": "2025-10-01T12:00:00",
    "ultimaConexion": "2025-10-17T19:05:00"
  }
  ```

- GET /api/usuarios/{id}/scrims-participando
  - Qué hace: Lista scrims donde el usuario tiene una postulacion aceptada.
  - Respuesta 200 (scrims donde el usuario tiene postulaciones ACEPTADAS)
  ```json
  [
    { "id": 10, "juego": {"id":1,"nombre":"Valorant"}, "region":"LATAM", "formato":"5v5", "estado":"LOBBY_ARMADO", "cuposTotal":10 }
  ]
  ```

- PATCH /api/usuarios/{id}
  - Qué hace: Actualiza campos editables del usuario (por ahora, región).
  - Body UpdateUsuarioRequest
  ```json
  { "region": "LATAM" }
  ```
  - Respuesta 200: usuario actualizado
  - Nota: Otros campos (mmr, rolPreferido, discordId, summoner, rol) no se modifican por este endpoint en esta versión.

- GET /api/usuarios/{id}/preferencias-notificacion
  - Qué hace: Obtiene las preferencias de notificación del usuario.
  - Respuesta 200
  ```json
  { "notifyPush": true, "notifyEmail": false, "notifyDiscord": true }
  ```

- PUT /api/usuarios/{id}/preferencias-notificacion
  - Qué hace: Actualiza las preferencias de notificación del usuario.
  - Body NotifPrefsRequest
  ```json
  { "notifyPush": true, "notifyEmail": false, "notifyDiscord": true }
  ```
  - Respuesta 200: usuario actualizado

## Scrims

- GET /api/scrims
  - Qué hace: Lista scrims; puede filtrar por juego, región, formato, rangos, latencia y fechas.
  - Query params opcionales: juego, region, formato, rangoMin, rangoMax, latenciaMax, fechaDesde, fechaHasta
  - Ejemplos:
    - /api/scrims
    - /api/scrims?juego=valorant&region=LATAM&formato=5v5&rangoMin=100&rangoMax=300&latenciaMax=80&fechaDesde=2025-10-18T00:00:00&fechaHasta=2025-10-20T00:00:00
  - Respuesta 200
  ```json
  [
    {
      "id": 10,
      "juego": { "id": 1, "nombre": "Valorant" },
      "region": "LATAM",
      "formato": "5v5",
      "rangoMin": 100,
      "rangoMax": 300,
      "latenciaMax": 80,
      "descripcion": "Entrenamiento semanal Valorant",
      "fechaHora": "2025-10-17T19:00:00",
      "fechaCreacion": "2025-10-16T20:00:00",
      "cuposTotal": 10,
      "duracionMinutos": 45,
      "estado": "BUSCANDO"
    }
  ]
  ```

- POST /api/scrims
  - Qué hace: Crea un nuevo scrim en estado BUSCANDO.
  - Body CrearScrimRequest
  ```json
  {
    "juegoId": 1,
    "region": "LATAM",
    "formato": "5v5",
    "rangoMin": 100,
    "rangoMax": 300,
    "latenciaMax": 80,
    "descripcion": "Entrenamiento semanal Valorant",
    "fechaHora": "2025-10-18T21:00:00",
    "cuposTotal": 10,
    "duracionMinutos": 60
  }
  ```
  - Respuesta 200
  ```json
  {
    "id": 11,
    "juego": { "id": 1, "nombre": "Valorant" },
    "region": "LATAM",
    "formato": "5v5",
    "rangoMin": 100,
    "rangoMax": 300,
    "latenciaMax": 80,
    "descripcion": "Entrenamiento semanal Valorant",
    "fechaHora": "2025-10-18T21:00:00",
    "fechaCreacion": "2025-10-17T18:55:00",
    "cuposTotal": 10,
    "duracionMinutos": 60,
    "estado": "BUSCANDO"
  }
  ```

- GET /api/scrims/{id}
  - Qué hace: Devuelve un detalle liviano del scrim.
  - Respuesta 200
  ```json
  { "id": 11, "estado": "BUSCANDO" }
  ```
  - Nota: Para obtener información detallada (postulaciones, confirmaciones y contadores), usá
    - GET /api/scrims/{id}/lobby (resumen con listas),
    - GET /api/scrims/{id}/postulaciones,
    - GET /api/scrims/{id}/confirmaciones.
    - GET /api/scrims/{id}/debug (diagnóstico: incluye `estado`, `cuposTotal` y `summary` con `pendientes/aceptadas/confirmadas`).

- POST /api/scrims/{id}/formar-equipos
  - Qué hace: Forma equipos para el scrim según la estrategia (por ahora, MMR). Devuelve objetos livianos.
  - Query param opcional: `estrategia` (default `POR_MMR`)
  - Respuesta 200
  ```json
  [
    { "id": 1001, "nombre": "Equipo A", "lado": "AZUL" },
    { "id": 1002, "nombre": "Equipo B", "lado": "ROJO" }
  ]
  ```
  - Notas:
    - El endpoint retorna sólo {id, nombre, lado} para evitar grafos profundos y facilitar la automatización de pruebas (Postman).
    - La formación admite escenarios con pocos jugadores (p. ej., 2) y alterna por MMR.

- GET /api/scrims/{id}/postulaciones
  - Qué hace: Lista las postulaciones del scrim.
  - Respuesta 200
  ```json
  [
    {
      "id": 100,
      "usuario": {"id":1,"username":"alice"},
      "rolDeseado":"DUELISTA",
      "comentario": "Juego entry",
      "fechaPostulacion": "2025-10-17T18:57:00",
      "estado":"ACEPTADA"
    }
  ]
  ```

- GET /api/scrims/{id}/confirmaciones
  - Qué hace: Lista las confirmaciones de asistencia del scrim.
  - Respuesta 200
  ```json
  [
    { "id": 200, "usuario": {"id":1,"username":"alice"}, "confirmado": true }
  ]
  ```

- GET /api/scrims/{id}/lobby
  - Qué hace: Muestra un resumen del lobby (pendientes, aceptadas, confirmadas) y listas asociadas.
  - Respuesta 200
  ```json
  {
    "scrimId": 11,
    "estado": "LOBBY_ARMADO",
    "cuposTotal": 10,
    "pendientes": 2,
    "aceptadas": 8,
    "confirmadas": 7,
    "postulaciones": [ { "id":100, "estado":"ACEPTADA" } ],
    "confirmaciones": [ { "id":200, "confirmado": true } ]
  }
  ```

- POST /api/scrims/{id}/postulaciones
  - Qué hace: Crea una nueva postulacion para el scrim; puede auto-aceptar hasta llenar cupos.
  - Body PostulacionRequest
  ```json
  { "usuarioId": 1, "rolDeseado": "MID", "comentario": "Juego entry" }
  ```
  - Respuesta 200
  ```json
  {
    "id": 100,
    "usuario": {"id":1},
  "rolDeseado":"MID",
    "comentario": "Juego entry",
    "fechaPostulacion": "2025-10-17T18:57:00",
    "estado":"ACEPTADA"
  }
  ```

- POST /api/scrims/{id}/confirmaciones
  - Qué hace: Registra la confirmación de asistencia; al completar cupos confirma el scrim.
  - Body ConfirmacionRequest
  ```json
  { "usuarioId": 1, "confirmado": true }
  ```
  - Respuesta 200
  ```json
  { "id": 200, "usuario": {"id":1}, "confirmado": true }
  ```

- POST /api/scrims/{id}/acciones/{command}
  - Qué hace: Ejecuta un comando de dominio (asignar rol, invitar, swap) sobre el scrim.
  - Ejecuta un comando de dominio (por ahora: asignarrol, invitarjugador, swapjugadores)
  - Body CommandRequest
  ```json
  { "actorId": 1, "payload": "{\"rol\":\"MID\"}" }
  ```
  - Respuesta 200 (ejemplo actual)
  ```json
  { "ok": true, "scrimId": 11, "actorId": 1, "payload": "{\"rol\":\"DUELIST\"}" }
  ```
- Waitlist
  - Qué hace: Gestiona lista de espera; permite agregar usuarios, listar y promover al primer lugar.
  - POST /api/scrims/{id}/waitlist?usuarioId=1 → agrega a lista de espera
  - GET /api/scrims/{id}/waitlist → lista
  - POST /api/scrims/{id}/waitlist/promover → mueve al primero de la waitlist a postulaciones aceptadas

- Feedback
  - Qué hace: Permite crear y listar feedback de un scrim (rating y comentario).
  - POST /api/scrims/{id}/feedback
  ```json
  { "autorId": 1, "rating": 5, "comentario": "GGs" }
  ```
  - GET /api/scrims/{id}/feedback

- Calendario
  - Qué hace: Genera un archivo .ics para agregar el scrim al calendario.
  - GET /api/scrims/{id}/calendar.ics → descarga evento iCal

## Reportes y Moderación

- POST /api/reportes
```json
{ "scrimId": 11, "reportadoId": 2, "motivo": "No-show" }
```
- GET /api/reportes?estado=PENDIENTE
- PUT /api/reportes/{id}/resolver
```json
{ "estado": "APROBADO", "sancion": "strike" }
```

> Acciones adicionales sobre Scrims

- POST /api/scrims/{id}/iniciar → 200: scrim con estado "EN_JUEGO".
  - Qué hace: Cambia el scrim de CONFIRMADO a EN_JUEGO.
- POST /api/scrims/{id}/matchmaking/run?strategy=mmr|latency|history → 200: scrim (puede pasar a "LOBBY_ARMADO").
  - Qué hace: Ejecuta la estrategia de matching; si completa los cupos, arma el lobby.
  - Opcional: Hay un scheduler que corre auto-matchmaking cada 5s. Este endpoint es útil para forcing/manual.
  - Idempotente: si ya está en LOBBY_ARMADO, re-ejecutarlo no produce error.
- DELETE /api/scrims/{id}/postulaciones/{postulacionId} → 204.
  - Qué hace: Retira una postulacion del scrim.
- POST /api/scrims/{id}/finalizar → 204.
  - Qué hace: Finaliza un scrim (marca estado FINALIZADO).
- POST /api/scrims/{id}/cancelar → 204.
  - Qué hace: Cancela un scrim (marca estado CANCELADO).
- POST /api/scrims/{id}/estadisticas
  - Qué hace: Registra estadísticas de un usuario en el scrim (MVP, observaciones).
  - Body
  ```json
  { "usuarioId": 1, "mvp": true, "observaciones": "Buen desempeño en retakes" }
  ```
  - Respuesta 200
  ```json
  { "id": 300, "usuario": {"id":1}, "mvp": true, "observaciones": "Buen desempeño en retakes" }
  ```

## Estadísticas

- GET /api/estadisticas/scrims/{id}
  - Qué hace: Lista estadísticas asociadas a un scrim.
  - Ejemplo: /api/estadisticas/scrims/11
  - Respuesta 200
  ```json
  [ { "id":300, "usuario": {"id":1}, "mvp": true, "observaciones": "..." } ]
  ```

- GET /api/estadisticas/usuarios/{id}
  - Qué hace: Lista estadísticas asociadas a un usuario.
  - Ejemplo: /api/estadisticas/usuarios/1
  - Respuesta 200
  ```json
  [ { "id":300, "scrim": {"id":11}, "mvp": true, "observaciones": "..." } ]
  ```

## Matches y Resultados

- GET /api/scrims/{id}/matches
  - Qué hace: Lista los matches asociados a un scrim (hoy 0..1, se devuelve lista por compatibilidad).
  - Respuesta 200
  ```json
  [ { "id": 500, "estado": "EN_PROGRESO", "fechaInicio": "2025-10-17T21:05:00" } ]
  ```

- POST /api/scrims/{id}/finalizar-match
  - Qué hace: Finaliza el match del scrim, setea ganador/perdedor y métricas agregadas; ajusta MMR por jugador y registra historial.
  - Body FinalizarMatchRequest
  ```json
  {
    "equipoGanadorId": 1,
    "duracionMinutos": 35,
    "killsGanador": 28,
    "killsPerdedor": 16,
    "torresGanador": 10,
    "torresPerdedor": 3,
    "goldDiff": 8500,
    "deltaWin": 15,
    "deltaLose": -12,
    "observaciones": "GG",
    "jugadores": [
      { "usuarioId": 101, "equipoId": 1, "kills": 10, "muertes": 2, "asistencias": 8, "minions": 230, "oro": 14000, "danoCausado": 32000, "danoRecibido": 9000, "torres": 2, "objetivos": 1 },
      { "usuarioId": 102, "equipoId": 2, "kills": 4, "muertes": 7, "asistencias": 5, "minions": 200, "oro": 11000, "danoCausado": 21000, "danoRecibido": 15000, "torres": 1, "objetivos": 0 }
    ]
  }
  ```
  - Respuesta 200: Match finalizado (incluye campos agregados)

- GET /api/matches/{matchId}/eventos
  - Qué hace: Lista eventos del match (kills, objetivos, pausas, etc.).
  - Respuesta 200
  ```json
  [ { "id": 700, "tipo": "KILL", "momento": "2025-10-17T21:07:00", "usuario": {"id":101} } ]
  ```

- GET /api/matches/{matchId}/estadisticas
  - Qué hace: Lista estadísticas por jugador del match.
  - Respuesta 200
  ```json
  [ { "usuario": {"id":101}, "kills": 10, "muertes": 2, "asistencias": 8 } ]
  ```

## Historial de Usuario

- GET /api/usuarios/{id}/historial
  - Qué hace: Lista historial de resultados del usuario (por match), incluyendo mmrAntes/mmrDespues y resultado.
  - Respuesta 200
  ```json
  [ { "match": {"id":500}, "resultado": "VICTORIA", "mmrAntes": 1200, "mmrDespues": 1215, "fechaRegistro": "2025-10-17T21:45:00" } ]
  ```

## Notificaciones

- GET /api/notificaciones?usuarioId={id}
  - Qué hace: Lista notificaciones del destinatario (ordenadas desc).
  - Respuesta 200
  ```json
  [ { "id": 400, "tipo": "SCRIM_STATE", "leida": false } ]
  ```
- GET /api/notificaciones/unread-count?usuarioId={id}
  - Qué hace: Devuelve el total de notificaciones no leídas del destinatario.
  - Respuesta 200
  ```json
  { "count": 3 }
  ```

- POST /api/notificaciones/{id}/leer
  - Qué hace: Marca una notificación como leída.
  - Respuesta 204

---

Notas:
- Fechas en formato ISO-8601 ("yyyy-MM-dd'T'HH:mm:ss").
- Campos como estado son enums String.
- La base se inicializa con `data.sql` (usuarios 1 y 2; juegos 1 y 2).

### Cómo arrancar la app rápidamente (sin MySQL)

- Perfil local con H2 en memoria:
  - Ejecutá con perfil `local` para usar H2 y datos semilla mínimos.
  - Comando (Windows):
    - PowerShell
      - `setx SPRING_PROFILES_ACTIVE local`
      - `.\n+mvnw.cmd spring-boot:run`
    - CMD
      - `set SPRING_PROFILES_ACTIVE=local && mvnw.cmd spring-boot:run`
  - Esto evita depender de MySQL y crea el schema en memoria automáticamente.

### Modelo enriquecido (referencia)

- Usuario
  - Nuevos/extendidos: mmr (number), rolPreferido (string), discordId (string|null), summoner (string|null), activo (boolean), fechaRegistro (datetime), ultimaConexion (datetime), rol (enum: USUARIO|MODERADOR|ADMINISTRADOR), verificacionEstado (enum: PENDIENTE|VERIFICADO|RECHAZADO).
  - Seguridad: passwordHash no se expone en las respuestas.
- Scrim
  - Nuevos: descripcion (string), fechaCreacion (datetime, set por el sistema), además de los existentes (region, formato, rangoMin/Max, latenciaMax, fechaHora, cuposTotal, duracionMinutos, estado).
- Postulacion
  - Nuevos: comentario (string opcional), fechaPostulacion (datetime, set por el sistema).
  - rolDeseado es un enum: [TOP, JUNGLE, MID, ADC, SUPPORT, FLEX].
