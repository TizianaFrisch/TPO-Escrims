package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.LogAuditoria;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.LogAuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {
    private final LogAuditoriaRepository repo;
    private final ObjectMapper om = new ObjectMapper();

    public AuditLogController(LogAuditoriaRepository repo) {
        this.repo = repo;
    }

    // List logs with optional filtering by entidad and entidadId. Restricted to moderators/admins.
    @GetMapping
    @PreAuthorize("hasAnyRole('MODERADOR','ADMINISTRADOR')")
    public Page<AuditLogDTO> list(
            @RequestParam(required = false) String entidad,
            @RequestParam(required = false) Long entidadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        var p = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Page<LogAuditoria> raw;
        if (entidad != null && entidadId != null) raw = repo.findByEntidadAndEntidadId(entidad, entidadId, p);
        else if (entidad != null) raw = repo.findByEntidad(entidad, p);
        else raw = repo.findAll(p);
        return raw.map(l -> AuditLogDTO.from(l, om));
    }

    @GetMapping("/entity/{entidad}/{entidadId}")
    @PreAuthorize("hasAnyRole('MODERADOR','ADMINISTRADOR')")
    public List<AuditLogDTO> byEntity(@PathVariable String entidad, @PathVariable Long entidadId) {
        return repo.findByEntidadAndEntidadIdOrderByTimestampDesc(entidad, entidadId)
                .stream().map(l -> AuditLogDTO.from(l, om)).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MODERADOR','ADMINISTRADOR')")
    public ResponseEntity<AuditLogDTO> getOne(@PathVariable Long id) {
        return repo.findById(id).map(l -> ResponseEntity.ok(AuditLogDTO.from(l, om))).orElse(ResponseEntity.notFound().build());
    }
}
