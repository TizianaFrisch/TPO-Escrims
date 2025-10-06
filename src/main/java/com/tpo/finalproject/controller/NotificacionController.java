package com.tpo.finalproject.controller;

import com.tpo.finalproject.domain.entities.Notificacion;
import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.service.NotificacionService;
import com.tpo.finalproject.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificacionController {
    
    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;
    
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerNotificacionesUsuario(@PathVariable Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            List<Notificacion> notificaciones = notificacionService.obtenerNotificacionesUsuario(usuario);
            return ResponseEntity.ok(notificaciones);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener notificaciones"));
        }
    }
    
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public ResponseEntity<?> obtenerNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            List<Notificacion> notificaciones = notificacionService.obtenerNotificacionesNoLeidas(usuario);
            return ResponseEntity.ok(notificaciones);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al obtener notificaciones no leídas"));
        }
    }
    
    @GetMapping("/usuario/{usuarioId}/count-no-leidas")
    public ResponseEntity<?> contarNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            Long count = notificacionService.contarNotificacionesNoLeidas(usuario);
            return ResponseEntity.ok(new CountResponse(count));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al contar notificaciones"));
        }
    }
    
    @PutMapping("/{notificacionId}/marcar-leida")
    public ResponseEntity<?> marcarComoLeida(@PathVariable Long notificacionId) {
        try {
            notificacionService.marcarComoLeida(notificacionId);
            return ResponseEntity.ok(new MessageResponse("Notificación marcada como leída"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al marcar notificación como leída"));
        }
    }
    
    @PutMapping("/usuario/{usuarioId}/marcar-todas-leidas")
    public ResponseEntity<?> marcarTodasComoLeidas(@PathVariable Long usuarioId) {
        try {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            notificacionService.marcarTodasComoLeidas(usuario);
            return ResponseEntity.ok(new MessageResponse("Todas las notificaciones marcadas como leídas"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al marcar notificaciones como leídas"));
        }
    }
    
    // DTOs
    
    public static class CountResponse {
        private Long count;
        
        public CountResponse(Long count) {
            this.count = count;
        }
        
        public Long getCount() { return count; }
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