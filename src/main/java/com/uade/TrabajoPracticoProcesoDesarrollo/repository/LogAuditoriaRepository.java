package com.uade.TrabajoPracticoProcesoDesarrollo.repository;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    // Métodos custom si se requieren
    java.util.List<LogAuditoria> findByEntidadOrderByTimestampDesc(String entidad);
    java.util.List<LogAuditoria> findByEntidadAndEntidadIdOrderByTimestampDesc(String entidad, Long entidadId);
    org.springframework.data.domain.Page<LogAuditoria> findByEntidad(String entidad, org.springframework.data.domain.Pageable p);
    org.springframework.data.domain.Page<LogAuditoria> findByEntidadAndEntidadId(String entidad, Long entidadId, org.springframework.data.domain.Pageable p);
}
