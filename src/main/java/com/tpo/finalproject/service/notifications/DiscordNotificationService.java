package com.tpo.finalproject.service.notifications;

import com.tpo.finalproject.config.DiscordProperties;
import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.domain.entities.Notificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;
import java.util.HashMap;

@Service
public class DiscordNotificationService {
    
    @Autowired
    private DiscordProperties discordProperties;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String DISCORD_API_BASE = "https://discord.com/api/v10";
    
    public void enviarMensajeDirecto(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
        if (!discordProperties.isEnabled() || discordProperties.getToken() == null || discordProperties.getToken().isEmpty() || usuario.getDiscordId() == null) {
            return;
        }
        
        try {
            // 1. Crear canal DM con el usuario
            String canalDmId = crearCanalDM(usuario.getDiscordId());
            
            if (canalDmId != null) {
                // 2. Enviar mensaje al canal DM
                enviarMensaje(canalDmId, construirMensaje(titulo, mensaje, tipo));
            }
            
        } catch (Exception e) {
            System.err.println("Error enviando mensaje Discord a " + usuario.getUsername() + ": " + e.getMessage());
        }
    }
    
    private String crearCanalDM(String discordUserId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(discordProperties.getToken());
            
            Map<String, String> payload = new HashMap<>();
            payload.put("recipient_id", discordUserId);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                DISCORD_API_BASE + "/users/@me/channels",
                request,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("id");
            }
            
        } catch (Exception e) {
            System.err.println("Error creando canal DM: " + e.getMessage());
        }
        
        return null;
    }
    
    private void enviarMensaje(String canalId, String mensaje) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(discordProperties.getToken());
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", mensaje);
            
            // Crear embed para mensaje más visual
            Map<String, Object> embed = new HashMap<>();
            embed.put("title", "🎮 eSports Platform");
            embed.put("description", mensaje);
            embed.put("color", 0x00ff00); // Verde
            embed.put("timestamp", java.time.Instant.now().toString());
            
            payload.put("embeds", new Object[]{embed});
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            restTemplate.postForEntity(
                DISCORD_API_BASE + "/channels/" + canalId + "/messages",
                request,
                Map.class
            );
            
        } catch (Exception e) {
            System.err.println("Error enviando mensaje: " + e.getMessage());
        }
    }
    
    private String construirMensaje(String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
        String emoji = obtenerEmojiPorTipo(tipo);
        return String.format("%s **%s**\n\n%s", emoji, titulo, mensaje);
    }
    
    private String obtenerEmojiPorTipo(Notificacion.TipoNotificacion tipo) {
        return switch (tipo) {
            case POSTULACION_ACEPTADA -> "✅";
            case POSTULACION_RECHAZADA -> "❌";
            case NUEVA_POSTULACION -> "📝";
            case EQUIPO_COMPLETO -> "👥";
            case SCRIM_CANCELADO -> "🚫";
            case REPORTE_RESUELTO -> "⚖️";
            default -> "🔔";
        };
    }
    
    public void enviarMensajeACanal(String canalId, String mensaje) {
        if (!discordProperties.isEnabled() || discordProperties.getToken() == null || discordProperties.getToken().isEmpty()) {
            return;
        }
        
        try {
            enviarMensaje(canalId, mensaje);
        } catch (Exception e) {
            System.err.println("Error enviando mensaje a canal: " + e.getMessage());
        }
    }
    
    public boolean validarBotToken() {
        if (!discordProperties.isEnabled() || discordProperties.getToken() == null || discordProperties.getToken().isEmpty()) {
            return false;
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(discordProperties.getToken());
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                DISCORD_API_BASE + "/users/@me",
                HttpMethod.GET,
                request,
                Map.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            System.err.println("Token de Discord inválido: " + e.getMessage());
            return false;
        }
    }
}