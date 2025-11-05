# Sistema de Scrims - TPO Proceso de Desarrollo

**Universidad Argentina de la Empresa (UADE)**  
Trabajo Práctico Obligatorio - Proceso de Desarrollo de Software

---

## Descripción

Sistema para organizar scrims (partidas competitivas de práctica) con **7 patrones de diseño** implementados, sistema de matchmaking con 4 estrategias, estadísticas MMR, y consola interactiva completa.

---

## Inicio Rápido (Consola Interactiva)

### 1. Requisitos Previos

- **Java 17** (verificar: `java -version`)
- **MySQL 8.0+** corriendo en puerto 3306
- **Git** para clonar el repositorio

### 2. Configurar Base de Datos

**Crear la base de datos en MySQL:**

```sql
CREATE DATABASE scrims_db;
```

**Configurar credenciales** en `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD_AQUI
```

### 3. Ejecutar la Aplicación

**Opción A - Windows (CMD o PowerShell):**

```cmd
mvnw.cmd spring-boot:run
```

**Opción B - Linux/Mac:**

```bash
./mvnw spring-boot:run
```

**Opción C - Desde tu IDE:**

- Abrir el proyecto en IntelliJ/Eclipse/VS Code
- Ejecutar `TrabajoPracticoProcesoDesarrolloApplication.java`

### 4. Usar la Consola

Una vez iniciada, verás el menú principal:

```
=== SISTEMA DE SCRIMS ===
1. Registrarse
2. Iniciar Sesion
3. Crear Scrim
...
```

**Flujo recomendado para probar:**

1. **Registrarse** (opción 1)
   - Crea tu usuario con email y contraseña
   - El sistema te verificará automáticamente

2. **Iniciar sesión** (opción 2)
   - Usa el email y contraseña que creaste

3. **Crear un scrim** (opción 3)
   - Elige juego (Valorant, LoL, CS2)
   - Selecciona formato (5v5 recomendado)
   - Elige estrategia de matchmaking (MMR, Latencia, Historial, Híbrida)

4. **Postularte a un scrim** (opción 5)
   - Ve los scrims disponibles
   - Postúlate al que creaste

5. **Confirmar participación** (opción 6)
   - Confirma que vas a jugar

6. **Finalizar scrim** (opción 8)
   - Genera equipos automáticamente
   - Crea estadísticas y ajusta MMR (+15 ganadores, -12 perdedores)

7. **Ver tu perfil** (opción 10)
   - Revisa tu MMR y rango actual
   - Ve tu historial de partidas

---

## Características Implementadas

### Patrones de Diseño (7)

1. **STATE** - Máquina de estados del scrim
   - Estados: BUSCANDO → LOBBY_ARMADO → CONFIRMADO → EN_JUEGO → FINALIZADO (+ CANCELADO)
   - Ubicación: `domain/state/`

2. **STRATEGY** - 4 estrategias de matchmaking
   - `ByMMRStrategy`: Equipos equilibrados por ranking
   - `ByLatencyStrategy`: Selección por menor ping
   - `ByHistoryStrategy`: Jugadores con historial (simulado por ID)
   - `HybridStrategy`: Combina múltiples factores
   - Ubicación: `matchmaking/`

3. **OBSERVER** - Sistema de notificaciones y eventos
   - `DomainEventBus` publica eventos
   - Subscribers reaccionan a cambios de estado
   - Ubicación: `notifications/`

4. **COMMAND** - Operaciones sobre scrims
   - `AsignarRolCommand`, `SwapJugadoresCommand`, `InvitarJugadorCommand`
   - Ubicación: `domain/commands/`

5. **BUILDER** - Construcción de scrims con validaciones
   - `ScrimBuilder` para crear scrims de forma fluida
   - Ubicación: `domain/builders/`

6. **FACTORY** - Creación de notificadores
   - `NotifierFactory` con implementaciones Dev/Prod
   - Ubicación: `notifications/`

7. **CHAIN OF RESPONSIBILITY** - Moderación de reportes
   - Cadena de handlers para aprobar/rechazar reportes
   - Ubicación: `service/ModeracionService.java`

### Funcionalidades Principales

- Sistema de MMR y rangos (Bronce, Plata, Oro, Platino, Diamante, Maestro, Gran Maestro)
- Matchmaking automático con 4 estrategias diferentes
- Formación automática de equipos balanceados
- Estadísticas detalladas por jugador y match
- Sistema de reportes y moderación
- Historial de partidas con evolución de MMR
- Notificaciones persistentes en base de datos

---

## Solución de Problemas

### Error: "Port 8080 already in use"

```powershell
# Windows PowerShell
Get-Process -Name java | Stop-Process -Force
```

### Error: "Access denied for user 'root'"

- Verifica que MySQL esté corriendo: `mysql -u root -p`
- Confirma usuario y contraseña en `application.properties`

### Error: "Table doesn't exist"

- La primera vez que ejecutes, Hibernate creará todas las tablas automáticamente
- El modo `create-drop` limpia la BD en cada reinicio (perfecto para testing)

### La consola no muestra bien los caracteres

✅ Ya está solucionado - todos los caracteres especiales fueron reemplazados por ASCII estándar

---

## API REST (Opcional)

Si preferís probar vía API en lugar de consola:

1. Cambiar en `application.properties`:
   ```properties
   spring.profiles.active=default
   ```

2. Importar `postman_collection.complete.json` en Postman

3. Ejecutar la colección completa (70+ requests con validaciones automáticas)

**Endpoints principales:**
- `POST /api/auth/register` - Registrar usuario
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/scrims` - Crear scrim
- `POST /api/scrims/{id}/postular` - Postularse
- `POST /api/scrims/{id}/finalizar` - Finalizar scrim
- `GET /api/usuarios/{id}/historial` - Ver historial con MMR

---

## Estructura del Proyecto

```
src/main/java/com/uade/TrabajoPracticoProcesoDesarrollo/
├── console/
│   └── ConsoleMenuRunner.java           # Menú interactivo (punto de entrada)
├── domain/
│   ├── state/                           # STATE Pattern
│   ├── commands/                        # COMMAND Pattern
│   ├── builders/                        # BUILDER Pattern
│   └── entities/                        # Entidades JPA (Scrim, Usuario, Match, etc.)
├── matchmaking/                         # STRATEGY Pattern (4 estrategias)
│   ├── ByMMRStrategy.java
│   ├── ByLatencyStrategy.java
│   ├── ByHistoryStrategy.java
│   └── HybridStrategy.java
├── notifications/                       # OBSERVER + FACTORY Pattern
│   ├── DomainEventBus.java
│   └── NotifierFactory.java
├── service/
│   ├── ScrimService.java                # Lógica principal
│   ├── MatchmakingService.java
│   └── ModeracionService.java           # CHAIN OF RESPONSIBILITY
└── web/                                 # Controllers REST (opcional)
```

---

## Tecnologías

- **Java 17**
- **Spring Boot 3.x**
  - Spring Web
  - Spring Data JPA
  - Spring Security + BCrypt
  - Spring Mail
- **MySQL 8.0**
- **JPA/Hibernate**
- **Maven**

---

## Tests

### Ejecutar Tests

```bash
mvnw test
```

**Tests incluidos:**
- `ScrimStateTransitionsTest` - Validación de transiciones de estado
- `ByMMRStrategyTest`, `ByLatencyStrategyTest`, `ByHistoryStrategyTest` - Tests de estrategias
- `AsignarRolCommandTest`, `InvitarSwapCommandsTest` - Tests de commands
- `ScrimBuilderTest` - Validación del builder
- Tests de integración de endpoints

### Colección Postman

Importar `postman_collection.complete.json` para:
- 70+ requests organizados en 8 categorías
- Validaciones automáticas con asserts
- Flujo completo end-to-end
- Testing de todos los patrones de diseño

---

## Autores

**UADE - Proceso de Desarrollo de Software 2025**

---

## Notas Importantes

- El sistema usa `create-drop` en modo consola: la BD se limpia en cada reinicio (ideal para testing)
- Los 3 juegos base (Valorant, League of Legends, Counter-Strike 2) se cargan automáticamente
- La verificación de email está desactivada para facilitar pruebas
- El sistema asigna latencia estimada por región automáticamente
- MMR inicial es 0, sube/baja +15/-12 según victorias/derrotas

---

**Para más detalles técnicos, revisar el código fuente o contactar al equipo de desarrollo.**
