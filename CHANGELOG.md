# Changelog

All notable changes to this feature branch are documented here.

## 2025-11-03

- Added Audit Log REST API
  - New controller: `web/AuditLogController.java`
  - New DTO: `web/AuditLogDTO.java`
  - Endpoints:
    - `GET /api/audit?limit={n}` → Últimos `n` eventos (default 50), ordenados por fecha desc
    - `GET /api/audit/{id}` → Detalle de un evento
- Added reminder notifications scheduler
  - New component: `notifications/NotificationsScheduler.java`
  - Job: revisa periódicamente scrims CONFIRMADO próximos a iniciar y envía recordatorios a usuarios confirmados usando `NotificacionService`
  - Usa `TipoNotificacion.CONFIRMADO` y `ConfirmacionRepository.findByScrimId(...)`
- Minor fixes
  - Null-safety y ordenamiento en AuditLogController
  - Alineación de repositorios y enums existentes en el proyecto

Notes:
- Scheduling ya estaba habilitado mediante `@EnableScheduling` en `TrabajoPracticoProcesoDesarrolloApplication`.
- Compilación y tests no verificados en este entorno; ejecutar localmente con `mvnw.cmd -DskipTests package` o `mvnw.cmd test`.
