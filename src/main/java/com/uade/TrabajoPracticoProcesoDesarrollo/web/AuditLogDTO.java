package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import java.time.LocalDateTime;

public record AuditLogDTO(
        Long id,
        String entidad,
        Long entidadId,
        String accion,
        String usuario,
        LocalDateTime timestamp,
        String detalles
) {}
