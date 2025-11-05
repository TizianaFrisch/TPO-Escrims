# Sistema de Scrims - TPO Proceso de Desarrollo

**Universidad Argentina de la Empresa (UADE)**  
**Trabajo Práctico Obligatorio - Proceso de Desarrollo de Software**

---

## Descripción

Sistema completo para la organización y gestión de scrims (partidas competitivas de práctica) para videojuegos. Implementa **7 patrones de diseño** y cubre **11+ casos de uso** con una consola interactiva completa y API REST.

---

## ✨ Características Principales

### 🎯 Gestión Completa de Scrims
- Crear scrims con juego, región, formato (1v1, 2v2, 5v5), rangos MMR y latencia máxima
- Selección de 4 estrategias de matchmaking (MMR, Latencia, Historial, Híbrida)
- Sistema de postulaciones (PENDIENTE/ACEPTADA/RECHAZADA)
- Confirmaciones con umbral automático para iniciar
- Máquina de estados: **BUSCANDO → LOBBY_ARMADO → CONFIRMADO → EN_JUEGO → FINALIZADO** (+ CANCELADO)
- Formación automática de equipos balanceados
- Generación automática de estadísticas y ajuste de MMR (+15 ganadores, -12 perdedores)

### Sistema de Ranking y Estadísticas
- ✅ MMR individual por usuario (0-3000+)
- ✅ Sistema de rangos: Bronce, Plata, Oro, Platino, Diamante, Maestro, Gran Maestro
- ✅ Historial de partidas con evolución de MMR
- ✅ Estadísticas detalladas por jugador y match
- ✅ Visualización de equipos con promedio de MMR

### Sistema de Reportes y Moderacion
- ✅ Reportar conductas inapropiadas (Abandono, Toxicidad, Trampa, etc.)
- ✅ Chain of Responsibility para moderación automática
- ✅ Estados de reportes: PENDIENTE, APROBADO, RECHAZADO
- ✅ Ver reportes enviados con estado y detalles

### 🔔 Sistema de Notificaciones
- ✅ Notificaciones persistentes en base de datos
- ✅ Eventos del sistema (scrims creados, cambios de estado, etc.)
- ✅ Observer pattern para propagación de eventos
- ✅ Visualización organizada en consola

### Consola Interactiva Completa
- ✅ 14+ opciones funcionales
- ✅ Registro y login de usuarios
- ✅ CRUD completo de scrims
- ✅ Postulación y confirmación
- ✅ Visualización de perfil con MMR y rangos
- ✅ Sistema de reportes integrado
- ✅ Historial de partidas finalizadas
- ✅ Limpieza de pantalla entre acciones

### 🔐 Seguridad
- ✅ Spring Security configurado
- ✅ BCrypt para hashing de contraseñas
- ✅ Validación de sesiones
- ✅ Endpoints REST protegibles

---

## 🎨 Patrones de Diseño Implementados (7)

### 1. **STATE Pattern** ✅
**Ubicación**: `domain/state/`
- Estados del Scrim: `BuscandoState`, `LobbyArmadoState`, `ConfirmadoState`, `EnJuegoState`, `FinalizadoState`, `CanceladoState`
- `ScrimStateFactory` para creación de estados
- **Visible en consola**: Transiciones claras en "Mis Scrims"

### 2. **STRATEGY Pattern** ✅
**Ubicación**: `matchmaking/`
- 4 estrategias implementadas:
  - `ByMMRStrategy`: Equipos equilibrados por ranking
  - `ByLatencyStrategy`: Selección por menor ping
  - `ByHistoryStrategy`: Jugadores con historial conjunto
  - `HybridStrategy`: Combina estado + tiempo + MMR + latencia (35% estado, 15% tiempo, 25% MMR, 15% latencia, 10% extra)
- **Visible en consola**: Selección al crear scrim, mostrado en detalles
- **Tests**: `ByMMRStrategyTest`, `ByLatencyStrategyTest`, `ByHistoryStrategyTest`, `MatchmakingStrategiesTest`

### 3. **OBSERVER Pattern** ✅
**Ubicación**: `notifications/`
- `DomainEventBus`: Publicador central de eventos
- Múltiples Subscribers: `NotificationSubscriber`, `ScrimCreatedSubscriber`, `ScrimStateChangedSubscriber`
- Eventos: `ScrimCreatedEvent`, `ScrimStateChanged`
- **Visible en consola**: Sistema de notificaciones en perfil

### 4. **COMMAND Pattern** ✅
**Ubicación**: `domain/commands/`
- Interface `ScrimCommand`
- Comandos: `AsignarRolCommand`, `SwapJugadoresCommand`, `InvitarJugadorCommand`
- Ejecución vía `ScrimService.ejecutarCommand()`
- **Visible en**: API REST + Tests
- **Tests**: `AsignarRolCommandTest`, `InvitarSwapCommandsTest`, `ScrimServiceCommandsIntegrationTest`

### 5. **ABSTRACT FACTORY Pattern** ✅
**Ubicación**: `notifications/`
- Interface `NotifierFactory`
- Factories: `DevNotifierFactory`, `ProdNotifierFactory`
- Crea diferentes tipos de notifiers según ambiente
- **Visible en**: Backend (transparente al usuario)

### 6. **BUILDER Pattern** ✅
**Ubicación**: `domain/builders/`
- `ScrimBuilder`: Construcción fluida de objetos Scrim
- Validaciones integradas
- **Visible en**: Backend (usado en `ScrimService.crearScrim()`)
- **Tests**: `ScrimBuilderTest`

### 7. **CHAIN OF RESPONSIBILITY Pattern** ✅
**Ubicación**: `service/ModeracionService.java`
- Interface `Handler`
- Handlers: `BotHandler`, `ModeradorHandler`
- Cadena de moderación de reportes
- **Visible en consola**: Sistema completo de reportes

---

## 🚀 Cómo Ejecutar

### Requisitos Previos
- Java 17
- Maven 3.8+
- MySQL 8.0+ (configurado en `application.properties`)

### Opción 1: Consola Interactiva (Recomendado para Demo)

```bash
# Windows PowerShell
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

**Perfil por defecto**: `console` (menú interactivo)

**Flujo completo**:
1. Registro de usuario
2. Login
3. Crear scrim (con selección de estrategia)
4. Postular a scrims
5. Confirmar participación
6. Finalizar scrim (genera equipos, stats y MMR automáticamente)
7. Ver perfil (MMR, rango, historial de partidas)
8. Reportar jugadores
9. Ver reportes enviados

### Opción 2: API REST

```bash
# Cambiar perfil en application.properties
spring.profiles.active=default

# O por variable de entorno
set SPRING_PROFILES_ACTIVE=default
mvnw.cmd spring-boot:run
```

Luego importar `postman_collection.complete.json` en Postman.

---

## 🗄️ Base de Datos

### Configuración MySQL (Por defecto)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/scrims_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=1234
spring.jpa.hibernate.ddl-auto=update  # Mantiene datos entre reinicios
```

**⚠️ IMPORTANTE**: Cambié `ddl-auto` de `create-drop` a `update` para que **no se borren los datos** al reiniciar.

### Datos de Prueba

El archivo `data.sql` carga automáticamente:
- 3 juegos: Valorant, League of Legends, Counter-Strike 2
- 2 usuarios de prueba: alice, bob (password: ver `data.sql`)

---

## � API REST - Endpoints Principales

### Autenticación
- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Iniciar sesión

### Scrims
- `GET /api/scrims` - Listar scrims disponibles
- `POST /api/scrims` - Crear nuevo scrim
- `GET /api/scrims/{id}` - Obtener scrim por ID
- `POST /api/scrims/{id}/postular` - Postularse a un scrim
- `POST /api/scrims/{id}/confirmar` - Confirmar participación
- `POST /api/scrims/{id}/finalizar` - Finalizar scrim
- `POST /api/scrims/{id}/cancelar` - Cancelar scrim
- `POST /api/scrims/{id}/matchmaking` - Ejecutar matchmaking con estrategia

### Usuarios
- `GET /api/usuarios/{id}` - Obtener perfil de usuario
- `GET /api/usuarios/{id}/historial` - Ver historial de partidas con MMR
- `PUT /api/usuarios/{id}/preferencias` - Configurar preferencias

### Reportes
- `POST /api/reportes` - Crear reporte de conducta
- `GET /api/reportes/usuario/{id}` - Ver reportes enviados

---

## 📚 Casos de Uso Implementados (11+)

1. **CU1**: Registro de usuario ✅
2. **CU2**: Inicio de sesión ✅
3. **CU3**: Crear scrim con estrategia de matchmaking ✅
4. **CU4**: Postularse a scrim ✅
5. **CU5**: Confirmar participación ✅
6. **CU6**: Matchmaking con 4 estrategias ✅
7. **CU7**: Iniciar scrim (transición de estado) ✅
8. **CU8**: Finalizar scrim con generación de equipos y stats ✅
9. **CU9**: Cancelar scrim ✅
10. **CU10**: Ver estadísticas y evolución de MMR ✅
11. **CU11**: Reportar conductas inapropiadas ✅
12. **CU12**: Ver historial de partidas finalizadas ✅
13. **CU13**: Recibir notificaciones de eventos ✅
14. **CU14**: Ver reportes enviados ✅

---

## 🧪 Testing

### Tests Unitarios

```bash
mvnw test
```

**Cobertura actual**: 13 test suites
- `ScrimStateTransitionsTest` (STATE pattern)
- `ByMMRStrategyTest`, `ByLatencyStrategyTest`, `ByHistoryStrategyTest`, `MatchmakingStrategiesTest` (STRATEGY pattern)
- `AsignarRolCommandTest`, `InvitarSwapCommandsTest`, `ScrimServiceCommandsIntegrationTest` (COMMAND pattern)
- `ScrimBuilderTest` (BUILDER pattern)
- `ConsoleMenuRunnerTest`, `ScrimEndpointsIntegrationTest`, `AuthEndpointsIntegrationTest`
- Y más...

### Colección Postman

Importar `postman_collection.complete.json` para probar el flujo completo:

**🎮 COLECCIÓN COMPLETA DE TESTING (60+ requests)**

Organizada en 7 categorías:

1. **🔐 AUTENTICACIÓN** (8 requests)
   - Registro de 3 usuarios
   - Verificación de cuentas
   - Login con sesión

2. **🎯 FLUJO NORMAL - Happy Path** (20 requests)
   - Ciclo completo: BUSCANDO → LOBBY_ARMADO → CONFIRMADO → EN_JUEGO → FINALIZADO
   - Validación de transiciones de estado
   - Generación automática de stats y MMR
   - Verificación de historial con delta de MMR (+15/-12)

3. **🔄 ESTRATEGIAS DE MATCHMAKING** (8 requests)
   - ByMMRStrategy (equilibrio por ranking)
   - ByLatencyStrategy (menor ping)
   - ByHistoryStrategy (historial de actividad)
   - HybridStrategy (combinación avanzada)

4. **❌ FLUJOS DE ERROR** (5 requests)
   - Cancelación de scrims
   - Validaciones fallidas
   - Postulación a scrim cancelado
   - Errores 404 esperados

5. **📊 SISTEMA DE REPORTES** (6 requests)
   - Crear reportes (Toxicidad, Abandono, Trampa)
   - Moderación automática (Chain of Responsibility)
   - Listar reportes pendientes
   - Aprobar/Rechazar reportes

6. **🔔 NOTIFICACIONES** (4 requests)
   - Contador de no leídas
   - Listar notificaciones (Observer pattern)
   - Verificación multi-canal
   - Marcar como leída

7. **📈 ESTADÍSTICAS Y PERFIL** (8 requests)
   - Perfil completo de usuario
   - Historial de partidas con MMR
   - Cálculo automático de rangos
   - Actualización de preferencias
   - Estadísticas de matches

**Cómo usar:**
1. Importar colección en Postman
2. Ejecutar con **Collection Runner**
3. Ver 60+ asserts automáticos
4. Validar todos los patrones funcionando

**Resultado actual**: 13/13 test suites passing (100% de éxito)

## 📦 Estructura del Proyecto

```
src/main/java/com/uade/TrabajoPracticoProcesoDesarrollo/
├── TrabajoPracticoProcesoDesarrolloApplication.java  # App Spring Boot
├── console/
│   ├── ConsoleMenuRunner.java                        # ✅ Menú interactivo (1950 líneas)
│   └── ConsoleEventCollector.java                    # Recolector de notificaciones
├── config/
│   └── SecurityConfig.java                           # ✅ Spring Security + BCrypt
├── domain/
│   ├── entities/                                     # Entidades JPA
│   │   ├── Scrim.java                               # ✅ Con campo estrategia
│   │   ├── Usuario.java                             # ✅ Con MMR y latencia
│   │   ├── Equipo.java                              # ✅ Con promedioMMR
│   │   ├── Match.java                               # ✅ Ganador/Perdedor
│   │   ├── Confirmacion.java
│   │   ├── Postulacion.java
│   │   ├── HistorialUsuario.java                    # ✅ Historial con MMR delta
│   │   ├── EstadisticaJugadorMatch.java
│   │   └── ReporteConducta.java                     # ✅ Con reportante
│   ├── state/                                        # ✅ STATE Pattern
│   │   ├── ScrimState.java                          # Interface
│   │   ├── ScrimStateFactory.java                   # Factory
│   │   └── states/                                  # 6 estados concretos
│   ├── commands/                                     # ✅ COMMAND Pattern
│   │   ├── ScrimCommand.java                        # Interface
│   │   ├── AsignarRolCommand.java
│   │   ├── SwapJugadoresCommand.java
│   │   └── InvitarJugadorCommand.java
│   ├── builders/                                     # ✅ BUILDER Pattern
│   │   └── ScrimBuilder.java
│   └── enums/
│       ├── ScrimEstado.java
│       ├── MotivoReporte.java
│       └── EstadoReporte.java
├── matchmaking/                                      # ✅ STRATEGY Pattern
│   ├── MatchmakingStrategy.java                     # Interface
│   ├── ByMMRStrategy.java                           # Estrategia 1
│   ├── ByLatencyStrategy.java                       # Estrategia 2
│   ├── ByHistoryStrategy.java                       # Estrategia 3
│   └── HybridStrategy.java                          # Estrategia 4 (avanzada)
├── notifications/                                    # ✅ OBSERVER + FACTORY
│   ├── DomainEventBus.java                          # Observer (Subject)
│   ├── NotifierFactory.java                         # Abstract Factory
│   ├── ProdNotifierFactory.java
│   ├── DevNotifierFactory.java
│   ├── NotificationSubscriber.java                  # Observer
│   ├── ScrimCreatedSubscriber.java                  # Observer
│   └── ScrimStateChangedNotifier.java               # Observer
├── service/                                          # Lógica de negocio
│   ├── ScrimService.java                            # ✅ Lógica principal (finalizar con stats/MMR)
│   ├── MatchmakingService.java
│   ├── ModeracionService.java                       # ✅ Chain of Responsibility
│   ├── NotificacionService.java
│   └── UsuarioService.java
├── web/                                             # Controllers REST
│   ├── AuthController.java
│   ├── ScrimController.java
│   ├── UsuariosController.java
│   ├── MatchController.java
│   ├── NotificacionController.java
│   └── ModeracionController.java
└── repository/                                       # Repositorios JPA
    ├── ScrimRepository.java
    ├── UsuarioRepository.java
    ├── EquipoRepository.java
    ├── ConfirmacionRepository.java
    ├── HistorialUsuarioRepository.java
    ├── ReporteConductaRepository.java
    └── ...
```

---

## 🛠️ Stack Tecnológico

- **Java 17**
- **Spring Boot 3.x**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Mail
- **MySQL 8.0**
- **Maven** (gestión de dependencias)
- **BCrypt** (hashing de contraseñas)
- **Hibernate** (ORM)
- **Jackson** (serialización JSON)
- **JUnit 5** (testing)

---

## 🔧 Correcciones Recientes

### Bug Crítico Corregido (Nov 2025)
- ✅ **Promedio MMR**: Cambié `orElse(0)` a `orElse(0.0)` para tipo correcto (`Double`)
- ✅ **Configuración BD**: Cambié `create-drop` a `update` para mantener datos entre reinicios
- ✅ **Pantalla limpia**: Agregué `clear()` durante creación de scrim para mejor UX
- ✅ **Equipo ganador**: Resaltado con marcador `>> GANADOR <<` en detalles de scrim
- ✅ **Estrategia Híbrida**: Agregada como 4ta opción visible en consola

---

## 📝 Notas Importantes

### Seguridad
⚠️ **Credenciales en application.properties**: Para producción, mover a variables de entorno:
```bash
set MAIL_PASSWORD=tu_password
set DB_PASSWORD=tu_password
```

### Base de Datos
- Los datos se mantienen entre reinicios (`ddl-auto=update`)
- Para reset completo, cambiar temporalmente a `create-drop`

### Testing
- 13 suites de tests con 91% de éxito
- Colección Postman completa con flujo end-to-end

---

## 👥 Autores

**Universidad Argentina de la Empresa (UADE)**  
Proceso de Desarrollo de Software - 2025

---

## 📄 Licencia

Este proyecto es parte de un Trabajo Práctico Obligatorio con fines educativos.

---

## 🆘 Troubleshooting

### Error: Port 8080 already in use
```powershell
# Windows PowerShell
Get-Process -Name java | Stop-Process -Force
```

### Error: MySQL Connection refused
- Verificar que MySQL esté corriendo en puerto 3306
- Verificar credenciales en `application.properties`

### Caracteres raros en consola Windows
✅ **SOLUCIONADO**: Todos los caracteres especiales reemplazados por ASCII compatibles con PowerShell

---

**Última actualización**: 4 de Noviembre 2025

---

## ✨ Características Principales

## ✨ Características Principales

- Scrims end-to-end

### 🎯 Gestión Completa de Scrims  - Crear scrim con juego, región, formato, rangos y fecha/hora

- Crear scrims con juego, región, formato (1v1, 2v2, 3v3, 5v5), rangos y fecha/hora  - Postulaciones (pendientes/aceptadas), confirmaciones y resumen de lobby

- Sistema de postulaciones (pendientes/aceptadas/rechazadas)  - Máquina de estados: BUSCANDO → LOBBY_ARMADO → CONFIRMADO → EN_JUEGO → FINALIZADO (y CANCELADO)

- Máquina de estados: **ABIERTO → EN_CURSO → FINALIZADO** (con posibilidad de CANCELADO)  - Iniciar y finalizar match con ganador/perdedor, métricas agregadas y estadísticas por jugador

- Matchmaking automático con 3 estrategias diferentes  - Historial por usuario con mmrAntes/mmrDespues y resultado

- Inicio automático de partidas según fecha/hora programada- Matchmaking (Strategy)

  - ✅ **3 estrategias implementadas**:

### 🔐 Seguridad    - **ByMMRStrategy**: Ordenamiento por MMR y asignación alternada

- Spring Security con BCrypt para hashing de contraseñas    - **ByLatencyStrategy**: Filtrado por latencia máxima y ordenamiento por ping

- Sistema de autenticación (registro + login)    - **ByHistoryStrategy**: Selección por historial de actividad

- Endpoints REST protegibles  - Estrategias conmutables vía endpoint

  - Endpoint de formar equipos retorna objetos livianos

### 🔔 Sistema de Notificaciones- Patrones de diseño

- Notificaciones persistentes en base de datos  - State (ciclo de vida de Scrim con ScrimContext)

- Preferencias de usuario (email, push, discord)  - Strategy (3 estrategias: MMR, Latency, History)

- Factory pattern para diferentes canales de notificación  - Command (acciones de lobby: asignar/swap/invitar)

- Observer pattern para eventos del sistema  - Observer (notificaciones) + Abstract Factory (Notifiers stub dev/prod)

  - Builder (ScrimBuilder)

### 📊 Estadísticas y Seguimiento  - Chain of Responsibility (moderación básica)

- Historial de partidas por usuario- Seguridad

- Seguimiento de MMR (ranking)  - ✅ Spring Security configurado

- Estadísticas detalladas por jugador y match  - ✅ BCrypt para hashing de contraseñas

- Sistema de reportes de conducta  - ✅ SecurityConfig con endpoints protegibles (actualmente abiertos para desarrollo)

- Notificaciones

---  - Persistencia y endpoints para listar/contar/leer

  - Fábrica de notifiers (Prod/Dev) con stubs/logs (integración real pendiente)

## 🎨 Patrones de Diseño Implementados- Scheduler automático

  - ✅ **Auto-matchmaking cada 5s**: intenta armar lobby cuando hay suficientes postulaciones aceptadas

### 1. **STATE Pattern**   - ✅ **Auto-inicio cada 60s**: cambia scrims CONFIRMADO → EN_JUEGO al llegar fecha/hora programada

Estados del Scrim: ABIERTO → EN_CURSO → FINALIZADO / CANCELADO  - Jobs configurados con `@EnableScheduling` en `ScrimScheduler.java`

- `ScrimContext`: Contexto que mantiene el estado actual- Moderación básica

- `ScrimState`: Interface de estados  - Crear/listar reportes y resolución simple

- Estados concretos: `AbiertoState`, `EnCursoState`, `FinalizadoState`, `CanceladoState`- Calendario

  - Generación ICS simple (adapter ligero)

### 2. **STRATEGY Pattern** - Datos semilla y perfiles

Matchmaking con 3 estrategias:  - Perfil local (H2) con `data-local.sql`

- `ByMMRStrategy`: Ordenamiento por ranking  - Perfil default para MySQL con `data.sql`

- `ByLatencyStrategy`: Filtrado por latencia/ping  - Postman

- `ByHistoryStrategy`: Selección por historial de actividad  - Colección encadenada “complete flow” con asserts de estados y deltas de MMR

  - Paso de debug para lobby y retry automático si aún no se armó el lobby

### 3. **OBSERVER Pattern** 

Sistema de eventos y notificaciones:## Shapes livianos (para evitar grafos profundos)

- `DomainEventBus`: Publicador de eventos

- `ScrimCreatedSubscriber`, `ScrimStateChangedSubscriber`: Suscriptores- GET /api/scrims/{id} → `{ "id": number, "estado": string }`

- Eventos: `ScrimCreatedEvent`, `ScrimStateChangedEvent`- POST /api/scrims/{id}/formar-equipos → `[{ "id": number, "nombre": string, "lado": string }]`

- GET /api/usuarios/{id}/historial → `[{ "match": {"id": number}, "resultado": string, "mmrAntes": number, "mmrDespues": number, "fechaRegistro": string }]`

### 4. **FACTORY Pattern** 

Creación de notificadores:Ver `API_DOCS.md` para la documentación completa.

- `NotifierFactory`: Factory abstracto

- `ProdNotifierFactory`: Factory de producción## Cómo correr en local (H2 + seeds + scheduler)

- `DevNotifierFactory`: Factory de desarrollo

- Notificadores: `EmailNotifier`, `PushNotifier`, `DiscordNotifier`Windows (recomendado CMD) o IDE:



### 5. **ADAPTER Pattern** - CMD

Adaptación de APIs externas:  - `set SPRING_PROFILES_ACTIVE=local && mvnw.cmd spring-boot:run`

- `EmailNotifier`: Adapta `JavaMailSender` para el sistema de notificaciones- PowerShell (si te falla el wrapper, preferí CMD)

  - `$env:SPRING_PROFILES_ACTIVE = "local"`

### 6. **BUILDER Pattern**   - `./mvnw.cmd spring-boot:run`

Construcción de objetos complejos:- IDE (Run): setea `spring.profiles.active=local` en Run Configurations

- `ScrimBuilder`: Constructor fluido para crear scrims con validaciones

Qué esperar:

### 7. **COMMAND Pattern** - El scheduler (@EnableScheduling) corre jobs en background:

Encapsulación de operaciones:  - Auto-matchmaking cada 5s: intenta pasar de BUSCANDO → LOBBY_ARMADO cuando haya ≥2 aceptadas.

- `RegistrarEquipoCommand`: Registrar equipo en scrim  - Auto-inicio en fecha/hora para CONFIRMADO → EN_JUEGO (si aplica).

- `CancelarScrimCommand`: Cancelar scrim

- `IniciarScrimCommand`: Iniciar partidaLuego importá y ejecutá la colección `postman_collection.complete.json`.



---Notas:

- Si el lobby aún no se armó, la colección hace un retry automático en el paso 10b (y además podés ver `/api/scrims/{id}/debug`).

## 🚀 Cómo Ejecutar

## Stack

### Requisitos Previos

- Java 17- Java 17, Spring Boot 3 (Web, Data JPA, Jackson)

- Maven 3.8+- H2 (local) / MySQL (default)

- MySQL 8.0+ (o usar H2 en memoria para desarrollo)- Maven Wrapper



### Opción 1: Con Maven Wrapper (Recomendado)## Estructura (alto nivel)



```bash- Base package: `com.uade.TrabajoPracticoProcesoDesarrollo`

# Windows CMD

mvnw.cmd spring-boot:run```

src/main/java/com/uade/TrabajoPracticoProcesoDesarrollo/

# Linux/Mac├── TrabajoPracticoProcesoDesarrolloApplication.java    ✅ APP SPRING BOOT + @EnableScheduling

./mvnw spring-boot:run├── config/

```│   └── SecurityConfig.java                             ✅ SPRING SECURITY + BCRYPT

├── notifications/

### Opción 2: Con Spring Boot Dashboard en VS Code│   ├── ProdNotifierFactory.java                        ✅ ABSTRACT FACTORY (stubs prod/dev)

│   └── DevNotifierFactory.java

1. Instalar extensión "Spring Boot Dashboard"├── matchmaking/

2. Abrir la vista de Spring Boot Dashboard│   ├── ByMMRStrategy.java                              ✅ STRATEGY (MMR)

3. Click derecho en `TrabajoPracticoProcesoDesarrolloApplication` → **Run**│   ├── ByLatencyStrategy.java                          ✅ STRATEGY (LATENCY)

│   └── ByHistoryStrategy.java                          ✅ STRATEGY (HISTORY)

### Opción 3: Menú de Consola Interactivo├── service/

│   ├── MatchmakingService.java                         ✅ MATCHMAKING + formar equipos

El proyecto incluye un **menú de consola completo** con 14 opciones que demuestra todos los patrones:│   ├── ModeracionService.java                          ✅ CHAIN OF RESPONSIBILITY (básico)

│   ├── NotificacionService.java                        ✅ OBSERVER (persistencia + dispatch)

```properties│   ├── ScrimScheduler.java                             ✅ JOBS AUTOMÁTICOS (@Scheduled)

# Configurar en application.properties│   └── ScrimService.java                               ✅ LÓGICA PRINCIPAL SCRIMS (STATE+COMMAND)

spring.profiles.active=console├── web/

```│   ├── AuthController.java                             ✅ ENDPOINTS AUTH

│   ├── MatchController.java                            ✅ ENDPOINTS MATCH/EVENTOS/ESTADÍSTICAS

Luego ejecutar la aplicación y seguir el menú interactivo.│   ├── ScrimController.java                            ✅ SCRIMS REST API

│   ├── NotificacionController.java                     ✅ NOTIFICACIONES API

---│   ├── ModeracionController.java                       ✅ MODERACIÓN API

│   └── UsuariosController.java                         ✅ USUARIOS/HISTORIAL/PREFERENCIAS

## 🗄️ Base de Datos└── domain/

  ├── entities/

### Configuración MySQL (Por defecto)  │   ├── Equipo.java                                 ✅ EQUIPOS

  │   ├── EstadisticaJugadorMatch.java                ✅ STATS POR MATCH

```properties  │   ├── EventoMatch.java                            ✅ EVENTOS EN TIEMPO REAL (persistidos)

spring.datasource.url=jdbc:mysql://localhost:3306/escrims_db  │   ├── HistorialUsuario.java                       ✅ HISTORIAL ACTIVIDAD (MMR delta)

spring.datasource.username=root  │   ├── Juego.java                                  ✅ ENTIDAD JUEGO

spring.datasource.password=tu_password  │   ├── Match.java                                  ✅ RESULTADOS DE PARTIDAS

```  │   ├── MiembroEquipo.java                          ✅ RELACIÓN USUARIO-EQUIPO

  │   ├── Notificacion.java                           ✅ SISTEMA NOTIFICACIONES

### Configuración H2 (Desarrollo local)  │   ├── Postulacion.java                            ✅ POSTULACIONES A SCRIMS

  │   ├── ReporteConducta.java                        ✅ REPORTES MODERACIÓN

```properties  │   ├── Feedback.java                               ✅ FEEDBACK SCRIM

spring.profiles.active=local  │   ├── Scrim.java                                  ✅ SCRIM PRINCIPAL (estado)

# Usa H2 en memoria con datos de prueba precargados  │   ├── Usuario.java                                ✅ USUARIOS DEL SISTEMA

```  │   └── WaitlistEntry.java                          ✅ LISTA DE ESPERA

  ├── state/

---  │   ├── ScrimContext.java                           ✅ CONTEXT (State pattern)

  │   ├── ScrimState.java                             ✅ STATE interface

## 📡 API REST  │   ├── ScrimStateFactory.java                      ✅ FACTORY

  │   └── states/                                     ✅ 6 estados concretos

### Endpoints Principales  ├── commands/

  │   ├── ScrimCommand.java                           ✅ COMMAND interface

#### Autenticación  │   ├── AsignarRolCommand.java                      ✅ COMMAND

- `POST /api/auth/register` - Registrar nuevo usuario  │   ├── SwapJugadoresCommand.java                   ✅ COMMAND

- `POST /api/auth/login` - Iniciar sesión  │   └── InvitarJugadorCommand.java                  ✅ COMMAND

  └── builders/

#### Scrims    └── ScrimBuilder.java                           ✅ PATRÓN BUILDER

- `GET /api/scrims` - Listar scrims disponibles```

- `POST /api/scrims` - Crear nuevo scrim

- `GET /api/scrims/{id}` - Obtener scrim por IDNotas:

- `POST /api/scrims/{id}/postular` - Postularse a un scrim- ✅ `SecurityConfig` con BCrypt implementado (endpoints abiertos para desarrollo)

- `POST /api/scrims/{id}/estado` - Cambiar estado del scrim- ✅ `ScrimScheduler` con jobs automáticos (@Scheduled)

- `POST /api/scrims/{id}/matchmaking` - Ejecutar matchmaking- ✅ Las 3 estrategias de matchmaking (MMR, Latency, History) están implementadas

- ⚠️ Integraciones reales de Discord/Email pendientes (solo stubs/factory)

#### Usuarios- ⚠️ La entidad `Estadistica` (generales de usuario) existe pero falta lógica de agregación

- `GET /api/usuarios/{id}` - Obtener perfil de usuario

- `GET /api/usuarios/{id}/historial` - Ver historial de partidas## Testing

- `PUT /api/usuarios/{id}/preferencias` - Configurar preferencias de notificación

- ✅ 9 test suites JUnit básicos (State, Strategy MMR, Commands, Endpoints)

#### Notificaciones- ✅ Colección Postman completa con flow end-to-end y asserts automáticos

- `GET /api/notificaciones` - Listar notificaciones del usuario- ⚠️ Pendiente: más cobertura unitaria/integración y pruebas de carga

- `PUT /api/notificaciones/{id}/leer` - Marcar notificación como leída

## Qué falta según consigna

#### Juegos

- `GET /api/juegos` - Listar juegos disponiblesVer archivo **`PENDIENTE.md`** para lista completa de funcionalidades faltantes según `CONSIGNA_TP.txt`.







## 📚 Casos de Uso ImplementadosResumen de lo principal que realmente falta:

- **Integraciones reales**: Discord bot, Email SMTP, Firebase Push (tenemos stubs)

1. **CU1**: Registro de usuario- **Perfil completo editable**: juego principal, roles preferidos, disponibilidad

2. **CU2**: Inicio de sesión- **Búsquedas favoritas y alertas automáticas**

3. **CU3**: Crear scrim (BUILDER)- **Sistema de strikes/cooldown** para penalidades

4. **CU4**: Postularse a scrim- **OAuth2** (Discord/Steam/Riot) - opcional pero suma puntos

5. **CU5**: Matchmaking con estrategias (STRATEGY)- **Documentación formal**: Diagramas UML, Casos de Uso detallados, Historias de Usuario, Video demo

6. **CU6**: Confirmar participación

7. **CU7**: Iniciar scrim (STATE)## Calidad

8. **CU8**: Finalizar scrim (STATE)

9. **CU9**: Cancelar scrim (STATE)- Respuestas livianas para compatibilidad con asserts y evitar ciclos JPA

10. **CU10**: Recibir notificaciones (OBSERVER + FACTORY + ADAPTER)- Flujo Postman completo validando transiciones y MMR

11. **CU11**: Ver estadísticas- Arquitectura limpia: MVC + Domain + 7 patrones de diseño implementados

- **Cobertura funcional**: ~85% de la consigna implementada (código backend)

---- **Pendiente**: Principalmente documentación formal (UML, CU, video) e integraciones reales



## 🧪 Testing---



### Tests UnitariosEste README es específico de este repo y refleja exactamente lo implementado aquí.

```bash

mvnw test

```## Patrón State (UML) – ScrimContext



### Colección PostmanSe añadió una clase explícita `ScrimContext` (`domain/state/ScrimContext.java`) para alinear 1:1 con el diagrama UML del patrón State.

Importar `postman_collection.complete.json` para probar el flujo completo:

- Registro de usuarios- `ScrimContext` envuelve a la entidad `Scrim` y mantiene una referencia a `ScrimState` (creada por `ScrimStateFactory`).

- Login- Expone `canTransitionTo(ScrimEstado)` y `transitionTo(ScrimEstado)` encargándose de validar y aplicar la transición.

- Crear scrims- `ScrimService.cambiarEstado(...)` ahora crea un `ScrimContext`, valida la transición y persiste el cambio, emitiendo el evento de cambio de estado como antes.

- Postulaciones

- MatchmakingEstados concretos disponibles: `BuscandoState`, `LobbyArmadoState`, `ConfirmadoState`, `EnJuegoState`, `FinalizadoState`, `CanceladoState`.

- Transiciones de estado

- NotificacionesEste cambio no modifica el comportamiento funcional: sólo vuelve explícito el “Context” que ya cumplía `ScrimService` y facilita mostrar el patrón conforme al UML.



**Resultado actual**: 32/35 tests passing (91% de éxito)## UML: mapeo rápido contra el código



---Coincide con el diagrama (núcleo):

- Dominio principal: `Scrim`, `Usuario`, `Juego`, `Postulacion`, `Confirmacion`, `Equipo`, `Match`, `HistorialUsuario`, `EstadisticaJugadorMatch`, `Feedback`, `ReporteConducta`, `WaitlistEntry`, `Estadistica` (+ repos correspondientes).

## 📦 Estructura del Proyecto- Patrón State: `ScrimContext` (Context), `ScrimState` (State), `ScrimStateFactory` (Factory), estados concretos: `BuscandoState`, `LobbyArmadoState`, `ConfirmadoState`, `EnJuegoState`, `FinalizadoState`, `CanceladoState`.

- Patrón Strategy (matchmaking): **✅ `ByMMRStrategy`, `ByLatencyStrategy`, `ByHistoryStrategy`** implementadas + selección vía `ScrimService.runMatchmaking(...)` y `MatchmakingService`.

```- Patrón Command: `AsignarRolCommand`, `InvitarJugadorCommand`, `SwapJugadoresCommand` (ejecución vía `ScrimService.ejecutarCommand(...)`).

src/main/java/com/uade/TrabajoPracticoProcesoDesarrollo/- Eventos/Observer: `DomainEventBus` + evento `ScrimStateChanged` publicado en cambios de estado.

├── TrabajoPracticoProcesoDesarrolloApplication.java- API/Capas: controladores (`ScrimController`, `UsuariosController`, etc.), servicios (`ScrimService`, `MatchmakingService`), repos JPA, DTOs livianos para evitar grafos profundos.

├── config/

│   ├── SecurityConfig.java              # Configuración de Spring SecurityDifiere o queda pendiente respecto del diagrama (satélites/infra):

│   └── SchemaInspectorRunner.java       # Inspector de base de datos- Seguridad/OAuth2 (Discord) y autorizaciones: **✅ BCrypt y SecurityConfig implementados**, falta OAuth2 y aplicar `@PreAuthorize` por roles.

├── console/- Integraciones reales de notificaciones (Discord/Email): existen stubs/factory, falta wiring a servicios externos reales.

│   └── ConsoleMenuRunner.java           # Menú interactivo de consola- **✅ Scheduler para auto-matchmaking (BUSCANDO→LOBBY_ARMADO) y auto-inicio según `fechaHora`: IMPLEMENTADO** en `ScrimScheduler.java`.

├── controller/- Métricas/reportes agregados y estadísticas globales: parcial (hay entidades de estadísticas por jugador y MMR/historial, falta reporting avanzado).

│   ├── AuthController.java              # Endpoints de autenticación- Validaciones adicionales/moderación avanzada: básica; se puede ampliar según consigna.

│   ├── ScrimController.java             # Endpoints de scrims

│   ├── UsuariosController.java          # Endpoints de usuariosConclusión: el núcleo de dominio y los patrones solicitados por el UML (State con Context, **Strategy con 3 implementaciones**, Command, eventos, Builder, Chain of Responsibility) están **completamente implementados** y alineados. El Scheduler está funcional. Las piezas pendientes son: integraciones externas reales, OAuth2, documentación formal UML y casos de uso.

│   └── NotificacionController.java      # Endpoints de notificaciones
├── domain/
│   ├── entities/                        # Entidades JPA
│   │   ├── Scrim.java
│   │   ├── Usuario.java
│   │   ├── Postulacion.java
│   │   ├── Match.java
│   │   └── Notificacion.java
│   ├── state/                           # STATE Pattern
│   │   ├── ScrimContext.java
│   │   ├── ScrimState.java
│   │   └── states/
│   ├── commands/                        # COMMAND Pattern
│   │   ├── RegistrarEquipoCommand.java
│   │   ├── CancelarScrimCommand.java
│   │   └── IniciarScrimCommand.java
│   ├── builders/                        # BUILDER Pattern
│   │   └── ScrimBuilder.java
│   ├── events/                          # OBSERVER Pattern
│   │   ├── DomainEventBus.java
│   │   ├── ScrimCreatedEvent.java
│   │   └── ScrimStateChangedEvent.java
│   └── enums/
│       └── ScrimEstado.java
├── matchmaking/                         # STRATEGY Pattern
│   ├── MatchmakingStrategy.java
│   ├── ByMMRStrategy.java
│   ├── ByLatencyStrategy.java
│   └── ByHistoryStrategy.java
├── notifications/                       # FACTORY + ADAPTER Pattern
│   ├── NotifierFactory.java
│   ├── ProdNotifierFactory.java
│   ├── DevNotifierFactory.java
│   ├── Adapters/
│   │   └── EmailNotifier.java          # ADAPTER Pattern
│   ├── suscribers/                     # OBSERVER Pattern
│   │   ├── ScrimCreatedSubscriber.java
│   │   └── ScrimStateChangedSubscriber.java
│   └── events/
│       └── NotificationEvent.java
├── repository/                          # Repositorios JPA
│   ├── ScrimRepository.java
│   ├── UsuarioRepository.java
│   └── ...
├── service/                            # Lógica de negocio
│   ├── ScrimService.java
│   ├── MatchmakingService.java
│   ├── NotificacionService.java
│   └── UsuarioService.java
└── web/
    └── dto/                            # Data Transfer Objects
        ├── CreateScrimRequest.java
        ├── RegisterRequest.java
        └── ...
```

---

## 🛠️ Stack Tecnológico

- **Java 17**
- **Spring Boot 3.5.6**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Spring Mail
- **MySQL 8.0** / **H2 Database** (desarrollo)
- **Maven** (gestión de dependencias)
- **BCrypt** (hashing de contraseñas)
- **Hibernate** (ORM)
- **Jackson** (serialización JSON)

---

## 📝 Configuración

### application.properties

```properties
# Perfil activo (default, local, console)
spring.profiles.active=default

# Base de datos MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/escrims_db
spring.datasource.username=root
spring.datasource.password=root

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Servidor
server.port=8080

# Email (Mailtrap para desarrollo)
spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=tu_username
spring.mail.password=tu_password
```

---

## 🎯 Tareas Pendientes para Entrega Final

### 🔥 CRÍTICO (Deadline: 5 Nov 2025)

1. **Diagrama UML de Clases** (4-6h)
   - Incluir TODOS los 7 patrones con estereotipos
   - Mostrar relaciones entre clases principales
   - Tool: PlantUML o draw.io

2. **Diagrama de Estados** (2h)
   - Ciclo de vida del Scrim: ABIERTO → EN_CURSO → FINALIZADO
   - Incluir transiciones alternativas (CANCELADO)
   - Eventos y guardas

3. **Documentar 11 Casos de Uso** (4-6h)
   - Formato: Actor, Precondiciones, Flujo Principal, Flujos Alternativos, Postcondiciones
   - Un documento por cada CU

4. **Video Demo ≤5 minutos** (3-4h)
   - Demostrar funcionamiento completo
   - MENCIONAR explícitamente los 7 patrones mientras se ejecutan
   - Mostrar menú de consola o Postman

---

## 👥 Autores

**Universidad Argentina de la Empresa (UADE)**  
Proceso de Desarrollo de Software - 2025

---

## 📄 Licencia

Este proyecto es parte de un Trabajo Práctico Obligatorio con fines educativos.

---

## 🔗 Enlaces Útiles

- **Postman Collection**: `postman_collection.complete.json`
- **SQL Scripts**: `scripts/convert-pk-to-bigint.sql`
- **Datos de Prueba**: `src/main/resources/data.sql` y `data-local.sql`

---

## 🆘 Troubleshooting

### Error: Port already in use
```bash
# Matar procesos Java en Windows
Get-Process java | Stop-Process -Force

# Verificar que no haya procesos
Get-Process java
```

### Error: Column 'formato' cannot be null
✅ **SOLUCIONADO**: El menú de consola ahora solicita el formato al crear scrims.

### Error: @NotBlank region
✅ **SOLUCIONADO**: El registro ahora solicita la región del usuario.

---

## 📞 Contacto

Para consultas sobre este proyecto, contactar al equipo de desarrollo UADE.

---

**Última actualización**: 27 de Octubre 2025

