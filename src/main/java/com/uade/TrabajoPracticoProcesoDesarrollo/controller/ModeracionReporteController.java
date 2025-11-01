package com.uade.TrabajoPracticoProcesoDesarrollo.controller;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.EstadoReporte;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ModeracionReporteService;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.exceptions.BusinessException;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('MODERADOR','ADMINISTRADOR')")
    public List<ReporteConducta> listarPendientes() {
        return service.listarPendientes();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MODERADOR','ADMINISTRADOR')")
    public ReporteConducta buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id).orElseThrow(() -> new BusinessException("Reporte no encontrado: " + id));
    }

    @PostMapping("/{id}/resolver")
    @PreAuthorize("hasAnyRole('MODERADOR','ADMINISTRADOR')")
    public void resolverReporte(@PathVariable Long id, @RequestParam String resolucion, @RequestParam EstadoReporte estado) {
        // delegate full resolution (including possible strikes) to service
        service.resolverReporte(id, estado, resolucion);
    }
}
