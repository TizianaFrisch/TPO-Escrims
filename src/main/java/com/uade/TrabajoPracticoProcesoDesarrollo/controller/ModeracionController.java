package com.uade.TrabajoPracticoProcesoDesarrollo.controller;

import java.time.LocalDateTime;
import java.util.Map;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.AuditLogService;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.DomainEventBus;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.exceptions.BusinessException;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.StrikeAppliedEvent;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

// Legacy moderation endpoints (kept for backward compatibility)
@RestController("legacyModeracionController")
@RequestMapping("/api/mod")
public class ModeracionController {
    private final UsuarioRepository usuarioRepo;
    private final AuditLogService audit;
    private final DomainEventBus bus;

    public ModeracionController(UsuarioRepository usuarioRepo, AuditLogService audit, DomainEventBus bus) {
        this.usuarioRepo = usuarioRepo;
        this.audit = audit;
        this.bus = bus;
    }

    private void checkCooldown(Usuario usuario) {
        if (usuario.getCooldownHasta() != null && usuario.getCooldownHasta().isAfter(LocalDateTime.now())) {
            throw new BusinessException("Cooldown activo hasta " + usuario.getCooldownHasta());
        }
    }

    // Moderación: registrar no-show
    @PostMapping("/no-show/{scrimId}/{userId}")
    @PreAuthorize("hasAnyRole('MODERADOR','ADMINISTRADOR')")
    public void registrarNoShow(@PathVariable Long scrimId, @PathVariable Long userId, @AuthenticationPrincipal Usuario adminUser) {
        Usuario usuario = usuarioRepo.findById(userId).orElseThrow(() -> new BusinessException("Usuario no encontrado: " + userId));
        checkCooldown(usuario);
        int strikes = usuario.getStrikes() != null ? usuario.getStrikes() : 0;
        strikes++;
        usuario.setStrikes(strikes);
        if (strikes >= 3) usuario.setCooldownHasta(LocalDateTime.now().plusDays(7));
        usuarioRepo.save(usuario);
    audit.log("Usuario", usuario.getId(), "no_show", adminUser.getUsername(), Map.of("scrimId", scrimId, "strikes", usuario.getStrikes()));
        bus.publish(new StrikeAppliedEvent(usuario.getId(), usuario.getStrikes()));
    }
}

