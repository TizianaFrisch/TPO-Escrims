package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Estadistica;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.EstadisticaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {
    private final EstadisticaRepository repo;

    public EstadisticasController(EstadisticaRepository repo) { this.repo = repo; }

    @GetMapping("/scrims/{id}")
    public List<Estadistica> porScrim(@PathVariable Long id) {
        return repo.findByScrimId(id);
    }

    @GetMapping("/usuarios/{id}")
    public List<Estadistica> porUsuario(@PathVariable Long id) {
        return repo.findByUsuarioId(id);
    }
}
