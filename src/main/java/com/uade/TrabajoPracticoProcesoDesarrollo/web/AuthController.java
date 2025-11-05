package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.NotifierFactory;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.ConfirmationService;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.LoginByEmailRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.LoginResponse;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationService confirmationService;
    private final NotifierFactory notifierFactory;
    @Value("${app.auth.require-verification:true}")
    private boolean requireVerification;

    public AuthController(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          ConfirmationService confirmationService,
                          NotifierFactory notifierFactory) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.confirmationService = confirmationService;
        this.notifierFactory = notifierFactory;
    }

    @PostMapping("/register")
    @SuppressWarnings("null")
    public ResponseEntity<Usuario> register(@Valid @RequestBody RegisterRequest req){
        // Username y email deben ser únicos
        if (usuarioRepository.findByUsername(req.username).isPresent()) {
            return ResponseEntity.status(409).build();
        }
        if (usuarioRepository.findByEmail(req.email).isPresent()) {
            return ResponseEntity.status(409).build();
        }

        Usuario u = new Usuario();
        u.setUsername(req.username);
        u.setNombre(req.username);
        u.setEmail(req.email);
        u.setPasswordHash(passwordEncoder.encode(req.password));
        u.setMmr(0);
        u.setRegion(req.region);
    u.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        // Set optional notification preferences if provided
        if (req.notifyPush != null) u.setNotifyPush(req.notifyPush);
        if (req.notifyEmail != null) u.setNotifyEmail(req.notifyEmail);
        if (req.notifyDiscord != null) u.setNotifyDiscord(req.notifyDiscord);

        var saved = usuarioRepository.save(u);

        // Si no se requiere verificación, marcar VERIFICADO y no generar token
        if (!requireVerification) {
            saved.setVerificacionEstado(VerificacionEstado.VERIFICADO);
            saved = usuarioRepository.save(saved);
            try { notifierFactory.createEmail().send(saved.getEmail(), "Registro exitoso: " + saved.getUsername()); } catch (Exception ignore) {}
        } else {
            // Generar token de confirmación y enviar emails
            String token = confirmationService.createForUser(saved);
            try {
                String confirmPath = "/api/auth/confirm?token=" + token;
                notifierFactory.createEmail().send(saved.getEmail(), "Registro exitoso: " + saved.getUsername());
                notifierFactory.createEmail().send(saved.getEmail(), "Confirmá tu cuenta: " + confirmPath);
            } catch (Exception ignore) {}
        }

    var location = URI.create("/api/usuarios/"+saved.getId());
    return ResponseEntity.created(location).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginByEmailRequest req){
        var uOpt = usuarioRepository.findByEmail(req.email);
        if (uOpt.isEmpty()) return ResponseEntity.status(401).build();
        var u = uOpt.get();
        if (!passwordEncoder.matches(req.password, u.getPasswordHash())) return ResponseEntity.status(401).build();
        if (requireVerification && u.getVerificacionEstado() != VerificacionEstado.VERIFICADO) {
            return ResponseEntity.status(403).build();
        }
        // Dev: sin JWT, devolvemos info mínima
        return ResponseEntity.ok(new LoginResponse(u.getId(), u.getUsername(), u.getVerificacionEstado()));
    }

    @GetMapping("/confirm")
    public ResponseEntity<LoginResponse> confirmByToken(@RequestParam("token") String token){
        try {
            var u = confirmationService.confirm(token);
            return ResponseEntity.ok(new LoginResponse(u.getId(), u.getUsername(), u.getVerificacionEstado()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(410).build();
        }
    }

    @PostMapping("/verify/{id}")
    public ResponseEntity<Usuario> verify(@PathVariable Long id){
    @SuppressWarnings("null")
    var u = usuarioRepository.findById(id).orElseThrow();
        u.setVerificacionEstado(VerificacionEstado.VERIFICADO);
        return ResponseEntity.ok(usuarioRepository.save(u));
    }
}
