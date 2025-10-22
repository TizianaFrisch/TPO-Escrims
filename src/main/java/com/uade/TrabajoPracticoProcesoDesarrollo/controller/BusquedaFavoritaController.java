package com.uade.TrabajoPracticoProcesoDesarrollo.controller;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.BusquedaFavoritaDTO;
// explicit imports kept minimal to avoid package wildcard resolution issues
import com.uade.TrabajoPracticoProcesoDesarrollo.model.BusquedaFavorita ;

import com.uade.TrabajoPracticoProcesoDesarrollo.repository.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/busquedas")
public class BusquedaFavoritaController {
    private final BusquedaFavoritaRepository repo;
    private final UsuarioRepository usuarioRepo;

    public BusquedaFavoritaController(BusquedaFavoritaRepository repo, UsuarioRepository usuarioRepo) {
        this.repo = repo; this.usuarioRepo = usuarioRepo;
    }

    @PostMapping
    public BusquedaFavorita create(@RequestBody @Valid BusquedaFavoritaDTO dto){
        Usuario u = usuarioRepo.findById(dto.getUsuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no existe"));
        var b = new BusquedaFavorita();
        b.setUsuario(u);
        b.setJuego(dto.getJuego());
        b.setRegion(dto.getRegion());
        b.setRangoMin(dto.getRangoMin());
        b.setRangoMax(dto.getRangoMax());
        b.setLatenciaMax(dto.getLatenciaMax());
        b.setAlertasActivas(dto.getAlertasActivas());
        return repo.save(b);
    }

    @GetMapping
    public List<BusquedaFavorita> mine(@RequestParam Long userId){
        return repo.findByUsuarioId(userId);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
