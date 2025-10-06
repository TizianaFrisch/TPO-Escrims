package com.tpo.finalproject.service.notifications;

import com.tpo.finalproject.config.AppProperties;
import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.domain.entities.Notificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailNotificationService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private AppProperties appProperties;
    
    @Value("${spring.mail.username:noreply@esports.com}")
    private String fromEmail;
    
    @Value("${app.name:eSports Platform}")
    private String appName;
    
    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void enviarNotificacionEmail(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
        if (!appProperties.getNotifications().getEmail().isEnabled() || usuario.getEmail() == null) {
            return;
        }
        
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail, appName);
            helper.setTo(usuario.getEmail());
            helper.setSubject(construirAsunto(titulo, tipo));
            helper.setText(construirMensajeHTML(usuario, titulo, mensaje, tipo), true);
            
            mailSender.send(mimeMessage);
            
        } catch (Exception e) {
            System.err.println("Error enviando email a " + usuario.getEmail() + ": " + e.getMessage());
            // Fallback a email simple
            enviarEmailSimple(usuario, titulo, mensaje);
        }
    }
    
    private void enviarEmailSimple(Usuario usuario, String titulo, String mensaje) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(usuario.getEmail());
            message.setSubject(titulo + " - " + appName);
            message.setText(construirMensajeTexto(usuario, titulo, mensaje));
            
            mailSender.send(message);
            
        } catch (Exception e) {
            System.err.println("Error enviando email simple: " + e.getMessage());
        }
    }
    
    private String construirAsunto(String titulo, Notificacion.TipoNotificacion tipo) {
        String emoji = obtenerEmojiPorTipo(tipo);
        return String.format("[%s] %s %s", appName, emoji, titulo);
    }
    
    private String construirMensajeHTML(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
        String emoji = obtenerEmojiPorTipo(tipo);
        String color = obtenerColorPorTipo(tipo);
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }
                    .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .header { background-color: %s; color: white; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }
                    .content { padding: 30px; }
                    .footer { background-color: #f8f9fa; padding: 20px; border-radius: 0 0 8px 8px; text-align: center; font-size: 12px; color: #666; }
                    .button { display: inline-block; padding: 12px 24px; background-color: %s; color: white; text-decoration: none; border-radius: 4px; margin-top: 20px; }
                    .highlight { background-color: #fff3cd; padding: 10px; border-left: 4px solid #ffc107; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s %s</h1>
                        <p>%s</p>
                    </div>
                    <div class="content">
                        <h2>Hola, %s!</h2>
                        <div class="highlight">
                            <p><strong>%s</strong></p>
                            <p>%s</p>
                        </div>
                        <p>Este mensaje fue generado automáticamente por nuestra plataforma.</p>
                        <a href="http://localhost:8080" class="button">Ir a la Plataforma</a>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 %s. Todos los derechos reservados.</p>
                        <p>Si no deseas recibir estos emails, puedes desactivar las notificaciones en tu perfil.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            titulo, color, color, emoji, titulo, appName, 
            usuario.getUsername(), titulo, mensaje, appName
        );
    }
    
    private String construirMensajeTexto(Usuario usuario, String titulo, String mensaje) {
        return String.format("""
            Hola %s,
            
            %s
            
            %s
            
            ---
            Este mensaje fue enviado automáticamente por %s.
            Si no deseas recibir estos emails, puedes desactivar las notificaciones en tu perfil.
            """,
            usuario.getUsername(), titulo, mensaje, appName
        );
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
    
    private String obtenerColorPorTipo(Notificacion.TipoNotificacion tipo) {
        return switch (tipo) {
            case POSTULACION_ACEPTADA -> "#28a745"; // Verde
            case POSTULACION_RECHAZADA -> "#dc3545"; // Rojo
            case NUEVA_POSTULACION -> "#007bff"; // Azul
            case EQUIPO_COMPLETO -> "#6f42c1"; // Púrpura
            case SCRIM_CANCELADO -> "#fd7e14"; // Naranja
            case REPORTE_RESUELTO -> "#20c997"; // Teal
            default -> "#6c757d"; // Gris
        };
    }
    
    public void enviarEmailRecuperacionPassword(Usuario usuario, String tokenRecuperacion) {
        if (!appProperties.getNotifications().getEmail().isEnabled()) return;
        
        try {
            String asunto = "[" + appName + "] Recuperación de Contraseña";
            String mensaje = String.format("""
                Hola %s,
                
                Has solicitado recuperar tu contraseña. Usa el siguiente enlace para restablecerla:
                
                http://localhost:8080/reset-password?token=%s
                
                Este enlace expira en 1 hora.
                
                Si no solicitaste esto, ignora este email.
                """,
                usuario.getUsername(), tokenRecuperacion
            );
            
            enviarEmailSimple(usuario, asunto, mensaje);
            
        } catch (Exception e) {
            System.err.println("Error enviando email de recuperación: " + e.getMessage());
        }
    }
    
    public void enviarEmailBienvenida(Usuario usuario) {
        if (!appProperties.getNotifications().getEmail().isEnabled()) return;
        
        String titulo = "¡Bienvenido a " + appName + "!";
        String mensaje = String.format("""
            ¡Hola %s!
            
            Tu cuenta ha sido creada exitosamente. Ya puedes:
            
            • Buscar y unirte a scrims
            • Crear tus propios scrims
            • Gestionar tu perfil y MMR
            • Conectar con otros jugadores
            
            ¡Que tengas buena suerte en la grieta!
            """,
            usuario.getUsername()
        );
        
        enviarNotificacionEmail(usuario, titulo, mensaje, Notificacion.TipoNotificacion.NUEVA_POSTULACION);
    }
}