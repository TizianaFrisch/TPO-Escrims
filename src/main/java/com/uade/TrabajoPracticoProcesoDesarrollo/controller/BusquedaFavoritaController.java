package com.uade.escrims.controller;

import com.uade.escrims.dto.BusquedaFavoritaDTO;
import com.uade.escrims.model.BusquedaFavorita;
import com.uade.escrims.model.Usuario;
import com.uade.escrims.repository.BusquedaFavoritaRepository;
import com.uade.escrims.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public List<BusquedaFavorita> mine(@RequestParam Long usuarioId){
        return repo.findByUsuarioId(usuarioId);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('MOD','ADMIN')")
    public void delete(@PathVariable Long id){ repo.deleteById(id); }
}
