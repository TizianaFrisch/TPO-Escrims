package com.uade.TrabajoPracticoProcesoDesarrollo.security;

import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.BusquedaFavoritaRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.model.BusquedaFavorita;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;

import java.util.Optional;

/**
 * Security helper bean used from SpEL (@PreAuthorize) to check ownership of
 * BusquedaFavorita resources. Mirrors pattern used in ScrimSecurity.
 */
@Component("busquedaFavoritaSecurity")
public class BusquedaFavoritaSecurity {
    private final BusquedaFavoritaRepository repo;
    private final UsuarioRepository usuarioRepo;

    public BusquedaFavoritaSecurity(BusquedaFavoritaRepository repo, UsuarioRepository usuarioRepo) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
    }

    public boolean isOwner(Long busquedaId, Authentication authentication) {
        if (busquedaId == null || authentication == null) return false;
        Optional<BusquedaFavorita> maybe = repo.findById(busquedaId);
        if (maybe.isEmpty()) return false;
        BusquedaFavorita b = maybe.get();

        Object principal = authentication.getPrincipal();
        if (principal instanceof Usuario u) {
            return u.getId() != null && u.getId().equals(b.getUsuario().getId());
        }

        // Fallback: try resolve by name (username/email)
        String name = authentication.getName();
        if (name == null) return false;
        var maybeUser = usuarioRepo.findByUsername(name);
        if (maybeUser.isEmpty()) maybeUser = usuarioRepo.findByEmail(name);
        return maybeUser.map(u -> u.getId() != null && u.getId().equals(b.getUsuario().getId())).orElse(false);
    }
}
