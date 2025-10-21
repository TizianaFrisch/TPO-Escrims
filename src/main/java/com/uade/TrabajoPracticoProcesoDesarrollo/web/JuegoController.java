package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.JuegoRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/juegos")
public class JuegoController {
    private final JuegoRepository repo;
    public JuegoController(JuegoRepository repo){ this.repo = repo; }

    @GetMapping
    public List<Juego> listar(){ return repo.findAll(); }

    @GetMapping("/{id}")
    public Juego obtener(@PathVariable Long id){ return repo.findById(id).orElseThrow(); }
}
