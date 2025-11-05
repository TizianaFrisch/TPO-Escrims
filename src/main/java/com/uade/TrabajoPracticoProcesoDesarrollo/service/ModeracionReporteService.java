package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ReporteConducta;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ReporteConductaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModeracionReporteService {
    private final ReporteConductaRepository repo;

    public ModeracionReporteService(ReporteConductaRepository repo) {
        this.repo = repo;
    }

    public List<ReporteConducta> listarPendientes() {
        return repo.findAll().stream().filter(r -> !Boolean.TRUE.equals(r.getResuelto())).toList();
    }

    public Optional<ReporteConducta> buscarPorId(Long id) {
        return repo.findById(id);
    }

    public void resolverReporte(Long id, String resolucion) {
        var reporte = repo.findById(id).orElseThrow();
        reporte.setResuelto(true);
        reporte.setResolucion(resolucion);
        repo.save(reporte);
    }
}
