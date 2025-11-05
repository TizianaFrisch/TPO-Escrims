package com.uade.TrabajoPracticoProcesoDesarrollo.controller;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ModeracionReporteService;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.exceptions.BusinessException;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/mod/reportes")
public class ModeracionReporteController {
    private final ModeracionReporteService service;

    public ModeracionReporteController(ModeracionReporteService service) {
        this.service = service;
    }

    @GetMapping("/pendientes")
    public List<ReporteConducta> listarPendientes() {
        return service.listarPendientes();
    }

    @GetMapping("/{id}")
    public ReporteConducta buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id).orElseThrow(() -> new BusinessException("Reporte no encontrado: " + id));
    }

    @PostMapping("/{id}/resolver")
    public void resolverReporte(@PathVariable Long id, @RequestParam String resolucion, @RequestParam EstadoReporte estado) {
        var reporte = service.buscarPorId(id).orElseThrow(() -> new BusinessException("Reporte no encontrado: " + id));
        reporte.setResolucion(resolucion);
        reporte.setResuelto(true);
        reporte.setEstado(estado);
        service.resolverReporte(id, resolucion);
    }
}
