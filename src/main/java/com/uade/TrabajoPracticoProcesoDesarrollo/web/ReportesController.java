package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ScrimService;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.CrearReporteRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.ResolverReporteRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReportesController {
    private final ScrimService scrimService;

    public ReportesController(ScrimService scrimService) { this.scrimService = scrimService; }

    @PostMapping
    public ReporteConducta crear(@RequestBody CrearReporteRequest req){
        return scrimService.crearReporte(req);
    }

    @GetMapping
    public List<ReporteConducta> listar(@RequestParam(required = false) EstadoReporte estado){
        return scrimService.listarReportes(estado);
    }

    @PutMapping("/{id}/resolver")
    public ReporteConducta resolver(@PathVariable Long id, @RequestBody ResolverReporteRequest req){
        return scrimService.resolverReporte(id, req);
    }
}
