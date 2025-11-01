package com.uade.TrabajoPracticoProcesoDesarrollo.security;

import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;

import java.util.Optional;

/**
 * Bean used by SpEL in @PreAuthorize expressions to check if the authenticated
 * user is the organizer/creator of a given scrim.
 *
 * Note: this implementation expects the Authentication.principal to be the
 * application Usuario entity (the project uses @AuthenticationPrincipal Usuario
 * in many controllers). If a different principal type is used, this method
 * will conservatively return false.
 */
@Component("scrimSecurity")
public class ScrimSecurity {
    private final ScrimRepository scrimRepo;
    private final UsuarioRepository usuarioRepo;

    public ScrimSecurity(ScrimRepository scrimRepo, UsuarioRepository usuarioRepo) {
        this.scrimRepo = scrimRepo;
        this.usuarioRepo = usuarioRepo;
    }

    public boolean isOrganizador(Long scrimId, Authentication authentication) {
        if (scrimId == null || authentication == null) return false;
        Optional<Scrim> maybe = scrimRepo.findById(scrimId);
        if (maybe.isEmpty()) return false;
        Scrim s = maybe.get();
        Object principal = authentication.getPrincipal();
        // If the security context holds the domain Usuario entity, use its id directly
        if (principal instanceof Usuario u) {
            if (s.getCreador() == null || s.getCreador().getId() == null) return false;
            return s.getCreador().getId().equals(u.getId());
        }

        // Otherwise, try to resolve the authenticated identity by name (username/email)
        String name = authentication.getName();
        if (name == null || name.isBlank()) return false;

        // Try lookup by username then by email
        Optional<Usuario> byUsername = usuarioRepo.findByUsername(name);
        if (byUsername.isPresent()) {
            Usuario found = byUsername.get();
            return s.getCreador() != null && s.getCreador().getId() != null && s.getCreador().getId().equals(found.getId());
        }

        Optional<Usuario> byEmail = usuarioRepo.findByEmail(name);
        if (byEmail.isPresent()) {
            Usuario found = byEmail.get();
            return s.getCreador() != null && s.getCreador().getId() != null && s.getCreador().getId().equals(found.getId());
        }

        // No matching Usuario found for current principal
        return false;
    }

    /**
     * Returns true if the scrim has no creator assigned (legacy/test data) or
     * the authenticated principal is the creator. This is useful for allowing
     * operations on unowned scrims when no creator information exists.
     */
    public boolean isOrganizadorOrUnowned(Long scrimId, Authentication authentication) {
        if (scrimId == null) return false;
        Optional<Scrim> maybe = scrimRepo.findById(scrimId);
        if (maybe.isEmpty()) return false;
        Scrim s = maybe.get();
        if (s.getCreador() == null || s.getCreador().getId() == null) return true; // unowned -> allow
        return isOrganizador(scrimId, authentication);
    }
}
