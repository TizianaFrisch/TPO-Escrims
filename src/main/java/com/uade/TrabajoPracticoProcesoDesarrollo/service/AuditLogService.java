package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

@Service
public class AuditLogService {
    private final ObjectMapper om = new ObjectMapper();
    private final JpaRepository<LogAuditoria, Long> repo;

    public AuditLogService(JpaRepository<LogAuditoria, Long> repo) {
        this.repo = repo;
    }

    public void log(String entidad, Long id, String accion, String usuario, Object detalles){
        var l = new LogAuditoria();
        l.setEntidad(entidad);
        l.setEntidadId(id);
        l.setAccion(accion);
        l.setUsuario(usuario);
        try {
            // domain entity stores detalles as plain text column named 'detalles'
            l.setDetalles(om.writeValueAsString(detalles));
        } catch(Exception ignored){}
        repo.save(l);
    }
}