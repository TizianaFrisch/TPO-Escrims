package com.uade.TrabajoPracticoProcesoDesarrollo.web;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.LoginByEmailRequest;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.LoginResponse;
import com.uade.TrabajoPracticoProcesoDesarrollo.web.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<Usuario> register(@Valid @RequestBody RegisterRequest req){
        // Derivar username del email si no fue provisto
        if (req.username == null || req.username.isBlank()) {
            req.username = req.email.substring(0, req.email.indexOf('@'));
        }
        if (usuarioRepository.findByUsername(req.username).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        if (usuarioRepository.findByEmail(req.email).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        Usuario u = new Usuario();
        u.setUsername(req.username);
        u.setEmail(req.email);
        u.setPasswordHash(passwordEncoder.encode(req.password));
        u.setRegion(req.region);
        u.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        var saved = usuarioRepository.save(u);
        return ResponseEntity.created(URI.create("/api/usuarios/"+saved.getId())).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginByEmailRequest req){
        var uOpt = usuarioRepository.findByEmail(req.email);
        if (uOpt.isEmpty()) return ResponseEntity.status(401).build();
        var u = uOpt.get();
        if (!passwordEncoder.matches(req.password, u.getPasswordHash())) return ResponseEntity.status(401).build();
        // Dev: sin JWT, devolvemos info mínima
        return ResponseEntity.ok(new LoginResponse(u.getId(), u.getUsername(), u.getVerificacionEstado()));
    }

    @PostMapping("/verify/{id}")
    public ResponseEntity<Usuario> verify(@PathVariable Long id){
        var u = usuarioRepository.findById(id).orElseThrow();
        u.setVerificacionEstado(VerificacionEstado.VERIFICADO);
        return ResponseEntity.ok(usuarioRepository.save(u));
    }
}
