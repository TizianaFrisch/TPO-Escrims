package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.LogAuditoria;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.LogAuditoriaRepository;
import java.time.LocalDateTime;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

@Service
public class AuditLogService {
    private final ObjectMapper om = new ObjectMapper();
    private final LogAuditoriaRepository repo;

    public AuditLogService(LogAuditoriaRepository repo) {
        this.repo = repo;
    }

    /**
     * Persist an audit log. If usuario is null or equals "system", try to resolve
     * the current authenticated principal name from the SecurityContext.
     */
    public void log(String entidad, Long id, String accion, String usuario, Object detalles){
        try {
            if (usuario == null || "system".equals(usuario)){
                Authentication a = SecurityContextHolder.getContext().getAuthentication();
                if (a != null && a.getName() != null && !a.getName().isBlank()) usuario = a.getName();
            }
        } catch(Exception ignored){}

        var l = new LogAuditoria();
        l.setEntidad(entidad);
        l.setEntidadId(id);
        l.setAccion(accion);
        l.setUsuario(usuario != null ? usuario : "system");
        l.setTimestamp(LocalDateTime.now());
        try {
            // domain entity stores detalles as plain text column named 'detalles'
            l.setDetalles(om.writeValueAsString(detalles));
        } catch(Exception ignored){}
        repo.save(l);
    }
}