
# eScrims Backend (Trabajo Práctico Proceso de Desarrollo)

Este README describe el estado REAL de este repositorio. No incluye integraciones o seguridad que existan en otros repos del equipo si aquí no están implementadas.

## Qué hace hoy (implementado)

- Scrims end-to-end
  - Crear scrim con juego, región, formato, rangos y fecha/hora
  - Postulaciones (pendientes/aceptadas), confirmaciones y resumen de lobby
  - Máquina de estados: BUSCANDO → LOBBY_ARMADO → CONFIRMADO → EN_JUEGO → FINALIZADO (y CANCELADO)
  - Iniciar y finalizar match con ganador/perdedor, métricas agregadas y estadísticas por jugador
  - Historial por usuario con mmrAntes/mmrDespues y resultado
- Matchmaking (Strategy)
  - ✅ **3 estrategias implementadas**:
    - **ByMMRStrategy**: Ordenamiento por MMR y asignación alternada
    - **ByLatencyStrategy**: Filtrado por latencia máxima y ordenamiento por ping
    - **ByHistoryStrategy**: Selección por historial de actividad
  - Estrategias conmutables vía endpoint
  - Endpoint de formar equipos retorna objetos livianos
- Patrones de diseño
  - State (ciclo de vida de Scrim con ScrimContext)
  - Strategy (3 estrategias: MMR, Latency, History)
  - Command (acciones de lobby: asignar/swap/invitar)
  - Observer (notificaciones) + Abstract Factory (Notifiers stub dev/prod)
  - Builder (ScrimBuilder)
  - Chain of Responsibility (moderación básica)
- Seguridad
  - ✅ Spring Security configurado
  - ✅ BCrypt para hashing de contraseñas
  - ✅ SecurityConfig con endpoints protegibles (actualmente abiertos para desarrollo)
- Notificaciones
  - Persistencia y endpoints para listar/contar/leer
  - Fábrica de notifiers (Prod/Dev) con stubs/logs (integración real pendiente)
- Scheduler automático
  - ✅ **Auto-matchmaking cada 5s**: intenta armar lobby cuando hay suficientes postulaciones aceptadas
  - ✅ **Auto-inicio cada 60s**: cambia scrims CONFIRMADO → EN_JUEGO al llegar fecha/hora programada
  - Jobs configurados con `@EnableScheduling` en `ScrimScheduler.java`
- Moderación básica
  - Crear/listar reportes y resolución simple
- Calendario
  - Generación ICS simple (adapter ligero)
- Datos semilla y perfiles
  - Perfil local (H2) con `data-local.sql`
  - Perfil default para MySQL con `data.sql`
  - Postman
  - Colección encadenada “complete flow” con asserts de estados y deltas de MMR
  - Paso de debug para lobby y retry automático si aún no se armó el lobby

## Shapes livianos (para evitar grafos profundos)

- GET /api/scrims/{id} → `{ "id": number, "estado": string }`
- POST /api/scrims/{id}/formar-equipos → `[{ "id": number, "nombre": string, "lado": string }]`
- GET /api/usuarios/{id}/historial → `[{ "match": {"id": number}, "resultado": string, "mmrAntes": number, "mmrDespues": number, "fechaRegistro": string }]`

Ver `API_DOCS.md` para la documentación completa.

## Cómo correr en local (H2 + seeds + scheduler)

Windows (recomendado CMD) o IDE:

- CMD
  - `set SPRING_PROFILES_ACTIVE=local && mvnw.cmd spring-boot:run`
- PowerShell (si te falla el wrapper, preferí CMD)
  - `$env:SPRING_PROFILES_ACTIVE = "local"`
  - `./mvnw.cmd spring-boot:run`
- IDE (Run): setea `spring.profiles.active=local` en Run Configurations

Qué esperar:
- El scheduler (@EnableScheduling) corre jobs en background:
  - Auto-matchmaking cada 5s: intenta pasar de BUSCANDO → LOBBY_ARMADO cuando haya ≥2 aceptadas.
  - Auto-inicio en fecha/hora para CONFIRMADO → EN_JUEGO (si aplica).

Luego importá y ejecutá la colección `postman_collection.complete.json`.

Notas:
- Si el lobby aún no se armó, la colección hace un retry automático en el paso 10b (y además podés ver `/api/scrims/{id}/debug`).

## Stack

- Java 17, Spring Boot 3 (Web, Data JPA, Jackson)
- H2 (local) / MySQL (default)
- Maven Wrapper

## Estructura (alto nivel)

- Base package: `com.uade.TrabajoPracticoProcesoDesarrollo`

```
src/main/java/com/uade/TrabajoPracticoProcesoDesarrollo/
├── TrabajoPracticoProcesoDesarrolloApplication.java    ✅ APP SPRING BOOT + @EnableScheduling
├── config/
│   └── SecurityConfig.java                             ✅ SPRING SECURITY + BCRYPT
├── notifications/
│   ├── ProdNotifierFactory.java                        ✅ ABSTRACT FACTORY (stubs prod/dev)
│   └── DevNotifierFactory.java
├── matchmaking/
│   ├── ByMMRStrategy.java                              ✅ STRATEGY (MMR)
│   ├── ByLatencyStrategy.java                          ✅ STRATEGY (LATENCY)
│   └── ByHistoryStrategy.java                          ✅ STRATEGY (HISTORY)
├── service/
│   ├── MatchmakingService.java                         ✅ MATCHMAKING + formar equipos
│   ├── ModeracionService.java                          ✅ CHAIN OF RESPONSIBILITY (básico)
│   ├── NotificacionService.java                        ✅ OBSERVER (persistencia + dispatch)
│   ├── ScrimScheduler.java                             ✅ JOBS AUTOMÁTICOS (@Scheduled)
│   └── ScrimService.java                               ✅ LÓGICA PRINCIPAL SCRIMS (STATE+COMMAND)
├── web/
│   ├── AuthController.java                             ✅ ENDPOINTS AUTH
│   ├── MatchController.java                            ✅ ENDPOINTS MATCH/EVENTOS/ESTADÍSTICAS
│   ├── ScrimController.java                            ✅ SCRIMS REST API
│   ├── NotificacionController.java                     ✅ NOTIFICACIONES API
│   ├── ModeracionController.java                       ✅ MODERACIÓN API
│   └── UsuariosController.java                         ✅ USUARIOS/HISTORIAL/PREFERENCIAS
└── domain/
  ├── entities/
  │   ├── Equipo.java                                 ✅ EQUIPOS
  │   ├── EstadisticaJugadorMatch.java                ✅ STATS POR MATCH
  │   ├── EventoMatch.java                            ✅ EVENTOS EN TIEMPO REAL (persistidos)
  │   ├── HistorialUsuario.java                       ✅ HISTORIAL ACTIVIDAD (MMR delta)
  │   ├── Juego.java                                  ✅ ENTIDAD JUEGO
  │   ├── Match.java                                  ✅ RESULTADOS DE PARTIDAS
  │   ├── MiembroEquipo.java                          ✅ RELACIÓN USUARIO-EQUIPO
  │   ├── Notificacion.java                           ✅ SISTEMA NOTIFICACIONES
  │   ├── Postulacion.java                            ✅ POSTULACIONES A SCRIMS
  │   ├── ReporteConducta.java                        ✅ REPORTES MODERACIÓN
  │   ├── Feedback.java                               ✅ FEEDBACK SCRIM
  │   ├── Scrim.java                                  ✅ SCRIM PRINCIPAL (estado)
  │   ├── Usuario.java                                ✅ USUARIOS DEL SISTEMA
  │   └── WaitlistEntry.java                          ✅ LISTA DE ESPERA
  ├── state/
  │   ├── ScrimContext.java                           ✅ CONTEXT (State pattern)
  │   ├── ScrimState.java                             ✅ STATE interface
  │   ├── ScrimStateFactory.java                      ✅ FACTORY
  │   └── states/                                     ✅ 6 estados concretos
  ├── commands/
  │   ├── ScrimCommand.java                           ✅ COMMAND interface
  │   ├── AsignarRolCommand.java                      ✅ COMMAND
  │   ├── SwapJugadoresCommand.java                   ✅ COMMAND
  │   └── InvitarJugadorCommand.java                  ✅ COMMAND
  └── builders/
    └── ScrimBuilder.java                           ✅ PATRÓN BUILDER
```

Notas:
- ✅ `SecurityConfig` con BCrypt implementado (endpoints abiertos para desarrollo)
- ✅ `ScrimScheduler` con jobs automáticos (@Scheduled)
- ✅ Las 3 estrategias de matchmaking (MMR, Latency, History) están implementadas
- ⚠️ Integraciones reales de Discord/Email pendientes (solo stubs/factory)
- ⚠️ La entidad `Estadistica` (generales de usuario) existe pero falta lógica de agregación

## Testing

- ✅ 9 test suites JUnit básicos (State, Strategy MMR, Commands, Endpoints)
- ✅ Colección Postman completa con flow end-to-end y asserts automáticos
- ⚠️ Pendiente: más cobertura unitaria/integración y pruebas de carga

## Qué falta según consigna

Ver archivo **`PENDIENTE.md`** para lista completa de funcionalidades faltantes según `CONSIGNA_TP.txt`.

**⚠️ NOTA IMPORTANTE**: Este README está actualizado al 19/10/2025. El archivo `PENDIENTE.md` puede estar desactualizado y no reflejar lo implementado aquí.

Resumen de lo principal que realmente falta:
- **Integraciones reales**: Discord bot, Email SMTP, Firebase Push (tenemos stubs)
- **Perfil completo editable**: juego principal, roles preferidos, disponibilidad
- **Búsquedas favoritas y alertas automáticas**
- **Sistema de strikes/cooldown** para penalidades
- **OAuth2** (Discord/Steam/Riot) - opcional pero suma puntos
- **Documentación formal**: Diagramas UML, Casos de Uso detallados, Historias de Usuario, Video demo

## Calidad

- Respuestas livianas para compatibilidad con asserts y evitar ciclos JPA
- Flujo Postman completo validando transiciones y MMR
- Arquitectura limpia: MVC + Domain + 7 patrones de diseño implementados
- **Cobertura funcional**: ~85% de la consigna implementada (código backend)
- **Pendiente**: Principalmente documentación formal (UML, CU, video) e integraciones reales

---

Este README es específico de este repo y refleja exactamente lo implementado aquí.


## Patrón State (UML) – ScrimContext

Se añadió una clase explícita `ScrimContext` (`domain/state/ScrimContext.java`) para alinear 1:1 con el diagrama UML del patrón State.

- `ScrimContext` envuelve a la entidad `Scrim` y mantiene una referencia a `ScrimState` (creada por `ScrimStateFactory`).
- Expone `canTransitionTo(ScrimEstado)` y `transitionTo(ScrimEstado)` encargándose de validar y aplicar la transición.
- `ScrimService.cambiarEstado(...)` ahora crea un `ScrimContext`, valida la transición y persiste el cambio, emitiendo el evento de cambio de estado como antes.

Estados concretos disponibles: `BuscandoState`, `LobbyArmadoState`, `ConfirmadoState`, `EnJuegoState`, `FinalizadoState`, `CanceladoState`.

Este cambio no modifica el comportamiento funcional: sólo vuelve explícito el “Context” que ya cumplía `ScrimService` y facilita mostrar el patrón conforme al UML.

## UML: mapeo rápido contra el código

Coincide con el diagrama (núcleo):
- Dominio principal: `Scrim`, `Usuario`, `Juego`, `Postulacion`, `Confirmacion`, `Equipo`, `Match`, `HistorialUsuario`, `EstadisticaJugadorMatch`, `Feedback`, `ReporteConducta`, `WaitlistEntry`, `Estadistica` (+ repos correspondientes).
- Patrón State: `ScrimContext` (Context), `ScrimState` (State), `ScrimStateFactory` (Factory), estados concretos: `BuscandoState`, `LobbyArmadoState`, `ConfirmadoState`, `EnJuegoState`, `FinalizadoState`, `CanceladoState`.
- Patrón Strategy (matchmaking): **✅ `ByMMRStrategy`, `ByLatencyStrategy`, `ByHistoryStrategy`** implementadas + selección vía `ScrimService.runMatchmaking(...)` y `MatchmakingService`.
- Patrón Command: `AsignarRolCommand`, `InvitarJugadorCommand`, `SwapJugadoresCommand` (ejecución vía `ScrimService.ejecutarCommand(...)`).
- Eventos/Observer: `DomainEventBus` + evento `ScrimStateChanged` publicado en cambios de estado.
- API/Capas: controladores (`ScrimController`, `UsuariosController`, etc.), servicios (`ScrimService`, `MatchmakingService`), repos JPA, DTOs livianos para evitar grafos profundos.

Difiere o queda pendiente respecto del diagrama (satélites/infra):
- Seguridad/OAuth2 (Discord) y autorizaciones: **✅ BCrypt y SecurityConfig implementados**, falta OAuth2 y aplicar `@PreAuthorize` por roles.
- Integraciones reales de notificaciones (Discord/Email): existen stubs/factory, falta wiring a servicios externos reales.
- **✅ Scheduler para auto-matchmaking (BUSCANDO→LOBBY_ARMADO) y auto-inicio según `fechaHora`: IMPLEMENTADO** en `ScrimScheduler.java`.
- Métricas/reportes agregados y estadísticas globales: parcial (hay entidades de estadísticas por jugador y MMR/historial, falta reporting avanzado).
- Validaciones adicionales/moderación avanzada: básica; se puede ampliar según consigna.

Conclusión: el núcleo de dominio y los patrones solicitados por el UML (State con Context, **Strategy con 3 implementaciones**, Command, eventos, Builder, Chain of Responsibility) están **completamente implementados** y alineados. El Scheduler está funcional. Las piezas pendientes son: integraciones externas reales, OAuth2, documentación formal UML y casos de uso.
