package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ModeracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moderacion")
public class ModeracionController {
    private final ModeracionService service;
    public ModeracionController(ModeracionService service){ this.service = service; }

    public static class CrearReporteRequest { public Long scrimId; public Long reportadoId; public String motivo; }
    public static class ResolverReporteRequest { public EstadoReporte estado; public String sancion; }

    @PostMapping("/reportes")
    public ResponseEntity<ReporteConducta> crear(@RequestBody CrearReporteRequest req){
        return ResponseEntity.ok(service.crear(req.scrimId, req.reportadoId, req.motivo));
    }

    @PutMapping("/reportes/{id}/resolver")
    public ResponseEntity<ReporteConducta> resolver(@PathVariable Long id, @RequestBody ResolverReporteRequest req){
        return ResponseEntity.ok(service.resolver(id, req.estado, req.sancion));
    }

    @GetMapping("/reportes/pendientes")
    public List<ReporteConducta> pendientes(){ return service.pendientes(); }

    @GetMapping("/reportes")
    public List<ReporteConducta> listar(@RequestParam(required = false) EstadoReporte estado){
        return service.listarPorEstado(estado);
    }
}
