package com.tpo.finalproject.controller;

import com.tpo.finalproject.service.notifications.DiscordNotificationService;
import com.tpo.finalproject.service.notifications.EmailNotificationService;
import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.domain.entities.Notificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestNotificationController {
    
    @Autowired
    private DiscordNotificationService discordService;
    
    @Autowired
    private EmailNotificationService emailService;
    
    @PostMapping("/discord")
    public ResponseEntity<String> testDiscord(@RequestBody Map<String, String> request) {
        try {
            String discordId = request.get("discordId");
            String mensaje = request.get("mensaje");
            
            if (discordId == null || mensaje == null) {
                return ResponseEntity.badRequest().body("Faltan parámetros: discordId, mensaje");
            }
            
            // Crear usuario temporal para test
            Usuario usuario = Usuario.builder()
                .username("test_user")
                .email("test@example.com")
                .discordId(discordId)
                .build();
            
            discordService.enviarMensajeDirecto(usuario, "Test Notification", mensaje, 
                Notificacion.TipoNotificacion.NUEVA_POSTULACION);
            
            return ResponseEntity.ok("✅ Mensaje Discord enviado exitosamente");
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Error enviando Discord: " + e.getMessage());
        }
    }
    
    @PostMapping("/email")
    public ResponseEntity<String> testEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String mensaje = request.get("mensaje");
            
            if (email == null || mensaje == null) {
                return ResponseEntity.badRequest().body("Faltan parámetros: email, mensaje");
            }
            
            // Crear usuario temporal para test
            Usuario usuario = Usuario.builder()
                .username("test_user")
                .email(email)
                .build();
            
            emailService.enviarNotificacionEmail(usuario, "Test Notification", mensaje, 
                Notificacion.TipoNotificacion.NUEVA_POSTULACION);
            
            return ResponseEntity.ok("✅ Email enviado exitosamente");
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Error enviando email: " + e.getMessage());
        }
    }
    
    @GetMapping("/discord/validate")
    public ResponseEntity<String> validateDiscordBot() {
        try {
            boolean isValid = discordService.validarBotToken();
            
            if (isValid) {
                return ResponseEntity.ok("✅ Discord Bot Token es válido y el bot está conectado");
            } else {
                return ResponseEntity.badRequest().body("❌ Discord Bot Token inválido o bot desconectado");
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Error validando Discord Bot: " + e.getMessage());
        }
    }
    
    @PostMapping("/send-welcome")
    public ResponseEntity<String> testWelcomeEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String username = request.get("username");
            
            if (email == null || username == null) {
                return ResponseEntity.badRequest().body("Faltan parámetros: email, username");
            }
            
            Usuario usuario = Usuario.builder()
                .username(username)
                .email(email)
                .build();
            
            emailService.enviarEmailBienvenida(usuario);
            
            return ResponseEntity.ok("✅ Email de bienvenida enviado exitosamente");
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("❌ Error enviando email de bienvenida: " + e.getMessage());
        }
    }
}