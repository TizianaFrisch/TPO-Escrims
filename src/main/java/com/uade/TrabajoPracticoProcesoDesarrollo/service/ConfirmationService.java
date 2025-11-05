package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.ConfirmationToken;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ConfirmationTokenRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ConfirmationService {
    private final ConfirmationTokenRepository tokenRepo;
    private final UsuarioRepository usuarioRepo;

    public ConfirmationService(ConfirmationTokenRepository tokenRepo, UsuarioRepository usuarioRepo) {
        this.tokenRepo = tokenRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @Transactional
    public String createForUser(Usuario u) {
        // invalidate previous tokens for this user
        try { tokenRepo.deleteByUsuario_Id(u.getId()); } catch (Exception ignore) {}
        ConfirmationToken t = new ConfirmationToken();
        t.setUsuario(u);
        t.setToken(UUID.randomUUID().toString().replace("-", ""));
        t.setExpiresAt(LocalDateTime.now().plusDays(2));
        tokenRepo.save(t);
        return t.getToken();
    }

    @Transactional
    public Usuario confirm(String token) {
        var t = tokenRepo.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Token inválido"));
        if (t.getUsedAt() != null) throw new IllegalStateException("Token ya utilizado");
        if (t.getExpiresAt() != null && t.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Token expirado");
        }
        var u = t.getUsuario();
        u.setVerificacionEstado(VerificacionEstado.VERIFICADO);
        usuarioRepo.save(u);
        t.setUsedAt(LocalDateTime.now());
        tokenRepo.save(t);
        return u;
    }
}
