package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.LogAuditoria;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.LogAuditoriaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {
    private final LogAuditoriaRepository repo;

    public AuditLogController(LogAuditoriaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<AuditLogDTO> list(@RequestParam(name = "limit", defaultValue = "100") int limit){
        var page = PageRequest.of(0, Math.min(Math.max(limit, 1), 500), Sort.by(Sort.Direction.DESC, "timestamp"));
        return repo.findAll(page).stream().map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogDTO> get(@PathVariable Long id){
        return repo.findById(Objects.requireNonNull(id))
                .map(e -> ResponseEntity.ok(toDto(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    private AuditLogDTO toDto(LogAuditoria e){
        return new AuditLogDTO(
                e.getId(),
                e.getEntidad(),
                e.getEntidadId(),
                e.getAccion(),
                e.getUsuario(),
                e.getTimestamp(),
                e.getDetalles()
        );
    }
}
