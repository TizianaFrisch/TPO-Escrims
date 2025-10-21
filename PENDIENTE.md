# 🔴 PENDIENTE - Lo que falta según CONSIGNA_TP.txt

**⚠️ ACTUALIZADO: 19 de Octubre, 2025**

Este documento lista **TODO LO QUE REALMENTE FALTA** implementar en el proyecto según la consigna oficial del TPO.

**IMPORTANTE**: Este proyecto tiene **~85% de la funcionalidad código implementada**. Las 3 estrategias de matchmaking, el scheduler completo y SecurityConfig con BCrypt YA ESTÁN IMPLEMENTADOS.

---

## ❌ FUNCIONALIDADES FALTANTES (CÓDIGO)

### ~~1. Estrategias de Matchmaking (Strategy Pattern)~~ ✅ **IMPLEMENTADO**

**Estado actual**: ✅ **LAS 3 ESTRATEGIAS ESTÁN IMPLEMENTADAS**
- ✅ `ByMMRStrategy.java` - Ordenamiento por MMR
- ✅ `ByLatencyStrategy.java` - Filtrado por latencia máxima
- ✅ `ByHistoryStrategy.java` - Selección por historial

**Ubicación**: `src/main/java/com/uade/.../matchmaking/`

---

### 2. Integraciones de Notificaciones Reales

**Estado actual**: Solo stubs/loggers (DevNotifierFactory/ProdNotifierFactory)  
**Falta implementar**:

#### Discord Bot/Webhook
```java
// 📍 Crear en: src/main/java/com/uade/.../notifications/adapters/
public class DiscordNotifier implements Notifier {
    // Enviar a webhook de Discord usando RestTemplate/WebClient
}
```

#### Email SMTP
```java
public class EmailNotifier implements Notifier {
    // Integrar JavaMail o SendGrid API
}
```

#### Push Notifications
```java
public class PushNotifier implements Notifier {
    // Integrar Firebase Cloud Messaging
}
```

**Impacto**: Consigna exige notificaciones multi-canal (push, email, Discord/Slack).

---

### ~~3. Scheduler Completo para Auto-Inicio~~ ✅ **IMPLEMENTADO**

**Estado actual**: ✅ **COMPLETAMENTE IMPLEMENTADO**
- ✅ `@EnableScheduling` activado en `TrabajoPracticoProcesoDesarrolloApplication.java`
- ✅ `ScrimScheduler.java` con 2 jobs:
  - **Auto-inicio** cada 60s: CONFIRMADO → EN_JUEGO
  - **Auto-matchmaking** cada 5s: BUSCANDO → LOBBY_ARMADO

**Ubicación**: `src/main/java/com/uade/.../service/ScrimScheduler.java`

---

### 4. Perfil de Usuario Completo Editable

**Estado actual**: Solo región es editable  
**Falta implementar**:

- Juego principal del usuario
- Roles preferidos (Duelist, Support, Jungla, etc.)
- Disponibilidad horaria (días/franjas)
- Endpoint `PUT /api/usuarios/{id}/perfil`

**Impacto**: Consigna pide perfil editable completo.

---

### 5. Búsquedas Favoritas y Alertas

**Estado actual**: No implementado  
**Falta implementar**:

```java
// 📍 Crear entidad: BusquedaFavorita
@Entity
public class BusquedaFavorita {
    @Id private Long id;
    @ManyToOne private Usuario usuario;
    private String juego;
    private String region;
    private Integer rangoMin;
    private Integer rangoMax;
    private Boolean alertasActivas;
}

// Al crear un scrim, verificar si coincide con búsquedas favoritas
// Enviar notificación (Observer) a usuarios con alertas activas
```

**Impacto**: Consigna pide guardar búsquedas y crear alertas (Observer).

---

### 6. Sistema de Strikes y Cooldown

**Estado actual**: No implementado  
**Falta implementar**:

```java
// 📍 Agregar a Usuario.java
private Integer strikes = 0;
private LocalDateTime cooldownHasta;

// Lógica:
// - Abandono/no-show → incrementar strikes
// - 3+ strikes → cooldown de X días
// - No permitir postulación si cooldown activo
```

**Impacto**: Consigna pide penalidades para reincidentes.

---

### 7. Autenticación OAuth2

**Estado actual**: Solo registro/login básico con email/contraseña  
**Falta implementar**:

- OAuth2 con Discord (Spring Security OAuth2 Client)
- OAuth2 con Steam (opcional)
- OAuth2 con Riot Games (opcional)

**Nota**: La consigna marca esto como "opcional", pero suma puntos.

---

## 🔒 SEGURIDAD Y NO FUNCIONALES FALTANTES

### ~~8. Seguridad Robusta~~ ⚠️ **PARCIALMENTE IMPLEMENTADO**

**Ya implementado**:
- ✅ **BCrypt**: `BCryptPasswordEncoder` configurado en `SecurityConfig.java`
- ✅ **Spring Security**: `@EnableWebSecurity` activo
- ✅ **SecurityConfig**: Configuración básica (endpoints abiertos para desarrollo)

**Falta implementar**:
- ❌ **Roles activos**: Aplicar `@PreAuthorize("hasRole('ADMIN')")` en endpoints sensibles
- ❌ **Rate Limiting**: Usar Bucket4j o similar para limitar requests por usuario
- ❌ **CORS/CSRF**: Configuración productiva

**Impacto**: Consigna exige roles (USER/MOD/ADMIN) activos y rate limiting.

---

### 9. Colas para Notificaciones

**Estado actual**: No implementado (consigna permite "simulado")  
**Falta implementar (opcional pero recomendado)**:

- RabbitMQ o Kafka para encolar notificaciones
- Evitar bloqueo del thread principal al enviar notificaciones
- Reintentos exponenciales si falla envío

**Impacto**: Mejora escalabilidad (req. no funcional).

---

### 10. Logs de Auditoría

**Estado actual**: No implementado  
**Falta implementar**:

```java
// 📍 Crear entidad: LogAuditoria
@Entity
public class LogAuditoria {
    @Id private Long id;
    private String entidad; // "Scrim", "ReporteConducta"
    private Long entidadId;
    private String accion; // "cambio_estado", "resolucion_reporte"
    private String usuario;
    private LocalDateTime timestamp;
    private String detalles; // JSON con antes/después
}
```

**Impacto**: Consigna pide trazabilidad para cambios de estado y moderación.

---

### 11. Pruebas de Carga

**Estado actual**: No medido  
**Falta implementar**:

- Test con JMeter o Gatling: 500 candidatos en matchmaking < 2s
- Validar que `ByMMRStrategy` cumple requisito de performance

**Impacto**: Consigna exige rendimiento < 2s para 500 candidatos.

---

### 12. Suite de Tests Completa

**Estado actual**: 9 test suites básicos (funcionan pero cobertura baja)  
**Falta ampliar**:

```
✅ Implementados (básicos):
- ScrimStateTransitionsTest
- ByMMRStrategyTest
- AsignarRolCommandTest
- InvitarSwapCommandsTest
- ScrimBuilderTest
- ScrimServiceCommandsIntegrationTest
- AuthEndpointsIntegrationTest
- ScrimEndpointsIntegrationTest
- ApplicationContextTest

❌ Faltan:
- ByLatencyStrategyTest
- ByHistoryStrategyTest
- NotificacionServiceTest (con mocks)
- MatchmakingServiceIntegrationTest
- ModeracionServiceTest
- SchedulerTest (verificar auto-inicio)
- SecurityTest (roles, OAuth2)
```

**Impacto**: Consigna exige unit tests, integration tests y tests de estado.

---

## 📄 DOCUMENTACIÓN FALTANTE (ENTREGABLES)

### 13. Diagrama de Clases UML

**Estado actual**: No incluido en repo  
**Falta crear**:

- Diagrama UML completo con estereotipos de patrones (<<State>>, <<Strategy>>, etc.)
- Mostrar: ScrimContext, ScrimState, MatchmakingStrategy, Commands, Notifiers, etc.
- Herramienta sugerida: StarUML, PlantUML, draw.io

**Impacto**: Entregable obligatorio según consigna.

---

### 14. Diagrama de Estados

**Estado actual**: Implementado en código, no documentado visualmente  
**Falta crear**:

- Diagrama de estados del Scrim (BUSCANDO → LOBBY_ARMADO → CONFIRMADO → EN_JUEGO → FINALIZADO / CANCELADO)
- Mostrar transiciones con eventos/condiciones

**Impacto**: Entregable obligatorio según consigna.

---

### 15. Casos de Uso Documentados

**Estado actual**: No incluidos como documento formal  
**Falta crear archivo `CASOS_DE_USO.md`**:

```
CU1 – Registrar usuario
- Actores: Usuario no registrado
- Precondiciones: Email no existe en sistema
- Flujo principal: [...]
- Postcondiciones: Usuario creado con estado Pendiente

CU2 – Autenticar usuario
CU3 – Crear scrim
CU4 – Postularse a scrim
CU5 – Emparejar y armar lobby (auto/manual)
CU6 – Confirmar participación
CU7 – Iniciar scrim (scheduler)
CU8 – Finalizar y cargar estadísticas
CU9 – Cancelar scrim
CU10 – Notificar eventos
CU11 – Moderar reportes
```

**Impacto**: Entregable obligatorio según consigna (con flujos, reglas de negocio, etc.).

---

### 16. Historias de Usuario

**Estado actual**: No incluidas como documento formal  
**Falta crear archivo `HISTORIAS_USUARIO.md`**:

```
HU1: Como jugador, quiero buscar scrims por rango y región para unirme a partidas con buen ping.
- Criterios de Aceptación:
  - Dado un scrim con rango [Gold–Plat], cuando un Player Silver se postula, entonces el sistema rechaza.
  - [...]

HU2: Como organizador, quiero crear un scrim 5v5 con límites de rango para equilibrar el lobby.
HU3: Como participante, quiero recibir notificaciones cuando el lobby se complete.
HU4: Como moderador, quiero procesar reportes con un flujo escalonado.
[...]
```

**Impacto**: Entregable recomendado según consigna.

---

### 17. Video Demo

**Estado actual**: No creado  
**Falta grabar**:

- Video ≤ 5 minutos mostrando:
  - Patrones en ejecución (State, Strategy, Command, Observer, etc.)
  - Flujo completo: crear scrim → postular → confirmar → match → finalizar
  - Notificaciones, estadísticas, historial
  - Código clave (ScrimContext, ByMMRStrategy, Commands, etc.)

**Impacto**: Entregable obligatorio según consigna.

---

## 📊 RESUMEN EJECUTIVO

### ✅ Lo que SÍ está implementado (COMPLETO):
1. ✅ Arquitectura MVC + Domain
2. ✅ Patrón State (6 estados + ScrimContext)
3. ✅ **Patrón Strategy (3 estrategias: MMR, Latency, History)**
4. ✅ Patrón Command (AsignarRol, Swap, Invitar)
5. ✅ Patrón Observer (DomainEventBus)
6. ✅ Patrón Abstract Factory (DevNotifierFactory/ProdNotifierFactory)
7. ✅ Patrón Builder (ScrimBuilder)
8. ✅ Patrón Chain of Responsibility (básico para moderación)
9. ✅ **Scheduler completo (auto-inicio + auto-matchmaking)**
10. ✅ **SecurityConfig con BCrypt**
11. ✅ API REST completa (40+ endpoints)
12. ✅ Persistencia JPA (H2/MySQL)
13. ✅ Tests básicos (9 suites)
14. ✅ Postman E2E con asserts

### ⚠️ Lo que está PARCIAL:
1. ⚠️ Notificaciones (persistencia ok, faltan integraciones reales Discord/Email/Push)
2. ⚠️ Seguridad (BCrypt ok, faltan roles activos + OAuth2 + rate limiting)
3. ⚠️ Perfil usuario (solo región editable, faltan juego/roles/disponibilidad)
4. ⚠️ Moderación (reportes ok, falta strikes/cooldown automático)
5. ⚠️ Testing (9 suites ok, falta cobertura amplia y tests para Latency/History)

### ❌ Lo que FALTA completamente:
1. ❌ Discord/Email/Push integraciones reales (tenemos stubs)
2. ❌ Búsquedas favoritas y alertas automáticas
3. ❌ Sistema de strikes/cooldown
4. ❌ OAuth2 (Discord/Steam/Riot)
5. ❌ Perfil completo (juego principal, roles, disponibilidad)
6. ❌ Roles activos con @PreAuthorize
7. ❌ Rate limiting (Bucket4j)
8. ❌ Colas de notificaciones (RabbitMQ/Kafka) - opcional
9. ❌ Logs de auditoría
10. ❌ Pruebas de carga (500 candidatos < 2s)
11. ❌ Tests para ByLatencyStrategy y ByHistoryStrategy
12. ❌ Diagrama UML de clases
13. ❌ Diagrama de estados visual
14. ❌ Casos de uso documentados
15. ❌ Historias de usuario documentadas
16. ❌ Video demo

---

## 🎯 PRIORIDADES PARA COMPLETAR EL TPO

### 🔥 CRÍTICAS (obligatorias para aprobar):
1. **Documentación formal**: UML, Casos de Uso, Video demo
2. ~~**Strategy completa**: ByLatencyStrategy + ByHistoryStrategy~~ ✅ **IMPLEMENTADO**
3. ~~**Scheduler completo**: auto-inicio de scrims~~ ✅ **IMPLEMENTADO**
4. **Tests ampliados**: Agregar tests para ByLatencyStrategy y ByHistoryStrategy, cobertura >70%

### ⚡ IMPORTANTES (mejoran nota):
5. **Integraciones reales**: Discord + Email (al menos mocks funcionales con llamadas HTTP)
6. ~~**Seguridad robusta**: BCrypt~~ ✅ **BCrypt implementado**, falta: roles activos + OAuth2 básico + rate limiting
7. **Búsquedas favoritas y alertas**
8. **Perfil completo editable**

### 💡 OPCIONALES (bonus):
8. **Colas de notificaciones** (RabbitMQ)
9. **Logs de auditoría**
10. **Pruebas de carga**
11. **Sistema de strikes/cooldown**

---

**Última actualización**: 19 de Octubre, 2025  
**Próxima revisión**: Comparar con avances del equipo antes de entrega 5/11/25
