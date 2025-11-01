package com.uade.TrabajoPracticoProcesoDesarrollo.controller;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.BusquedaGuardada;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.BusquedaGuardadaRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios/{usuarioId}/busquedas")
public class BusquedaGuardadaController {
    private final BusquedaGuardadaRepository repo;
    private final UsuarioRepository usuarioRepo;

    public BusquedaGuardadaController(BusquedaGuardadaRepository repo, UsuarioRepository usuarioRepo) {
        this.repo = repo; this.usuarioRepo = usuarioRepo;
    }

    @PreAuthorize("@principalResolver.isCurrentUser(#usuarioId, authentication) or hasRole('ADMINISTRADOR')")
    @PostMapping
    public BusquedaGuardada crear(@PathVariable Long usuarioId, @RequestBody BusquedaGuardada dto) {
        var usuario = usuarioRepo.findById(usuarioId).orElseThrow();
        dto.setUsuario(usuario);
        return repo.save(dto);
    }

    @PreAuthorize("@principalResolver.isCurrentUser(#usuarioId, authentication) or hasRole('ADMINISTRADOR')")
    @GetMapping
    public List<BusquedaGuardada> listar(@PathVariable Long usuarioId) {
        return repo.findByUsuarioId(usuarioId);
    }

    @PreAuthorize("@principalResolver.isCurrentUser(#usuarioId, authentication) or hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{busquedaId}")
    public void eliminar(@PathVariable Long usuarioId, @PathVariable Long busquedaId) {
        var b = repo.findById(busquedaId).orElseThrow();
        if (!b.getUsuario().getId().equals(usuarioId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        repo.deleteById(busquedaId);
    }
}
