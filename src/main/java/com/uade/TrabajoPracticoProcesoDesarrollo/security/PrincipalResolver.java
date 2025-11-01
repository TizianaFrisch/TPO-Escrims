package com.uade.TrabajoPracticoProcesoDesarrollo.security;

import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;

import java.util.Optional;

/**
 * Small helper bean to resolve the current authenticated application's user id
 * in SpEL expressions and programmatic checks. Supports when Authentication.principal
 * is the domain Usuario entity or when only username/email is available.
 */
@Component("principalResolver")
public class PrincipalResolver {
    private final UsuarioRepository usuarioRepo;

    public PrincipalResolver(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    public Long resolveUserId(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof Usuario u) {
            return u.getId();
        }
        String name = authentication.getName();
        if (name == null) return null;
        Optional<com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario> maybe = usuarioRepo.findByUsername(name);
        if (maybe.isEmpty()) maybe = usuarioRepo.findByEmail(name);
        return maybe.map(com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario::getId).orElse(null);
    }

    public boolean isCurrentUser(Long userId, Authentication authentication) {
        if (userId == null) return false;
        Long resolved = resolveUserId(authentication);
        return resolved != null && resolved.equals(userId);
    }
}
