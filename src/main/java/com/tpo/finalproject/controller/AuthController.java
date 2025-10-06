package com.tpo.finalproject.controller;

import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<?> registrarUsuario(@RequestBody RegistroRequest request) {
        try {
            Usuario usuario = authService.registrarUsuario(
                    request.getUsername(),
                    request.getEmail(),
                    request.getDiscordId(),
                    request.getSummoner(),
                    request.getRegion(),
                    request.getMmr()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegistroResponse("Usuario registrado exitosamente", usuario.getId()));
                    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error interno del servidor"));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> autenticarUsuario(@RequestBody LoginRequest request) {
        try {
            Optional<Usuario> usuario = authService.autenticarUsuario(request.getUsername());
            
            if (usuario.isPresent() && usuario.get().getActivo()) {
                authService.actualizarUltimaConexion(usuario.get().getId());
                
                return ResponseEntity.ok(new LoginResponse(
                        "Autenticación exitosa",
                        usuario.get().getId(),
                        usuario.get().getUsername(),
                        usuario.get().getRol().name()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Credenciales inválidas o usuario inactivo"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error en la autenticación"));
        }
    }
    
    @PostMapping("/discord-login")
    public ResponseEntity<?> autenticarConDiscord(@RequestBody DiscordLoginRequest request) {
        try {
            Optional<Usuario> usuario = authService.obtenerUsuarioPorDiscordId(request.getDiscordId());
            
            if (usuario.isPresent() && usuario.get().getActivo()) {
                authService.actualizarUltimaConexion(usuario.get().getId());
                
                return ResponseEntity.ok(new LoginResponse(
                        "Autenticación con Discord exitosa",
                        usuario.get().getId(),
                        usuario.get().getUsername(),
                        usuario.get().getRol().name()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Usuario de Discord no encontrado o inactivo"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error en la autenticación con Discord"));
        }
    }
    
    @GetMapping("/usuarios-activos")
    public ResponseEntity<List<Usuario>> obtenerUsuariosActivos() {
        try {
            List<Usuario> usuarios = authService.obtenerUsuariosActivos();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/desactivar/{usuarioId}")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long usuarioId) {
        try {
            authService.desactivarUsuario(usuarioId);
            return ResponseEntity.ok(new MessageResponse("Usuario desactivado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al desactivar usuario"));
        }
    }
    
    // DTOs
    
    public static class RegistroRequest {
        private String username;
        private String email;
        private String discordId;
        private String summoner;
        private String region;
        private Integer mmr;
        
        // Getters y setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDiscordId() { return discordId; }
        public void setDiscordId(String discordId) { this.discordId = discordId; }
        public String getSummoner() { return summoner; }
        public void setSummoner(String summoner) { this.summoner = summoner; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public Integer getMmr() { return mmr; }
        public void setMmr(Integer mmr) { this.mmr = mmr; }
    }
    
    public static class LoginRequest {
        private String username;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
    
    public static class DiscordLoginRequest {
        private String discordId;
        
        public String getDiscordId() { return discordId; }
        public void setDiscordId(String discordId) { this.discordId = discordId; }
    }
    
    public static class RegistroResponse {
        private String mensaje;
        private Long usuarioId;
        
        public RegistroResponse(String mensaje, Long usuarioId) {
            this.mensaje = mensaje;
            this.usuarioId = usuarioId;
        }
        
        public String getMensaje() { return mensaje; }
        public Long getUsuarioId() { return usuarioId; }
    }
    
    public static class LoginResponse {
        private String mensaje;
        private Long usuarioId;
        private String username;
        private String rol;
        
        public LoginResponse(String mensaje, Long usuarioId, String username, String rol) {
            this.mensaje = mensaje;
            this.usuarioId = usuarioId;
            this.username = username;
            this.rol = rol;
        }
        
        public String getMensaje() { return mensaje; }
        public Long getUsuarioId() { return usuarioId; }
        public String getUsername() { return username; }
        public String getRol() { return rol; }
    }
    
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() { return error; }
    }
    
    public static class MessageResponse {
        private String mensaje;
        
        public MessageResponse(String mensaje) {
            this.mensaje = mensaje;
        }
        
        public String getMensaje() { return mensaje; }
    }
}