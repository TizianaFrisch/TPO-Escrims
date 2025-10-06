package com.tpo.finalproject.service;

import com.tpo.finalproject.domain.entities.Notificacion;
import com.tpo.finalproject.domain.entities.Usuario;
import com.tpo.finalproject.repository.NotificacionRepository;
import com.tpo.finalproject.service.notifications.DiscordNotificationService;
import com.tpo.finalproject.service.notifications.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class NotificacionService {
    
    private final NotificacionRepository notificacionRepository;
    private final DiscordNotificationService discordService;
    private final EmailNotificationService emailService;
    
    @Value("${app.notifications.discord.enabled:false}")
    private boolean discordEnabled;
    
    @Value("${app.notifications.email.enabled:false}")
    private boolean emailEnabled;
    
    // ============== PATRÓN FACTORY INTEGRADO ==============
    // Factory para crear notificadores específicos
    
    public interface Notificador {
        void enviar(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo);
        String getTipo();
        boolean isDisponible();
    }
    
    // Notificador Push integrado
    private class PushNotificador implements Notificador {
        @Override
        public void enviar(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
            // Simulación de notificación push
            System.out.println("📱 PUSH to " + usuario.getUsername() + ": " + titulo + " - " + mensaje);
            
            // Aquí se integraría con servicios reales como:
            // - Firebase Cloud Messaging (FCM)
            // - Apple Push Notification Service (APNs)
            // - OneSignal, Pusher, etc.
        }
        
        @Override
        public String getTipo() {
            return "PUSH";
        }
        
        @Override
        public boolean isDisponible() {
            return true; // Siempre disponible para móviles
        }
    }
    
    // Notificador WebSocket integrado
    private class WebSocketNotificador implements Notificador {
        @Override
        public void enviar(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
            // Simulación de notificación en tiempo real
            System.out.println("🌐 WEBSOCKET to " + usuario.getUsername() + ": " + titulo + " - " + mensaje);
            
            // Aquí se integraría con:
            // - Spring WebSocket
            // - Socket.IO
            // - SockJS
        }
        
        @Override
        public String getTipo() {
            return "WEBSOCKET";
        }
        
        @Override
        public boolean isDisponible() {
            return true; // Siempre disponible para web
        }
    }
    
    // Factory para crear notificadores
    public class NotificadorFactory {
        
        public Notificador crearNotificador(String tipo) {
            switch (tipo.toUpperCase()) {
                case "DISCORD":
                    return new DiscordNotificadorWrapper();
                case "EMAIL":
                    return new EmailNotificadorWrapper();
                case "PUSH":
                    return new PushNotificador();
                case "WEBSOCKET":
                    return new WebSocketNotificador();
                default:
                    throw new IllegalArgumentException("Tipo de notificador no soportado: " + tipo);
            }
        }
        
        public List<Notificador> crearTodosLosNotificadores() {
            List<Notificador> notificadores = new ArrayList<>();
            notificadores.add(new DiscordNotificadorWrapper());
            notificadores.add(new EmailNotificadorWrapper());
            notificadores.add(new PushNotificador());
            notificadores.add(new WebSocketNotificador());
            return notificadores;
        }
    }
    
    // Wrappers para integrar servicios existentes
    private class DiscordNotificadorWrapper implements Notificador {
        @Override
        public void enviar(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
            if (isDisponible() && usuario.getDiscordId() != null) {
                try {
                    discordService.enviarMensajeDirecto(usuario, titulo, mensaje, tipo);
                } catch (Exception e) {
                    System.err.println("Error enviando Discord: " + e.getMessage());
                }
            }
        }
        
        @Override
        public String getTipo() {
            return "DISCORD";
        }
        
        @Override
        public boolean isDisponible() {
            return discordEnabled;
        }
    }
    
    private class EmailNotificadorWrapper implements Notificador {
        @Override
        public void enviar(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
            if (isDisponible() && usuario.getEmail() != null) {
                try {
                    emailService.enviarNotificacionEmail(usuario, titulo, mensaje, tipo);
                } catch (Exception e) {
                    System.err.println("Error enviando Email: " + e.getMessage());
                }
            }
        }
        
        @Override
        public String getTipo() {
            return "EMAIL";
        }
        
        @Override
        public boolean isDisponible() {
            return emailEnabled;
        }
    }
    
    // Factory instance
    private final NotificadorFactory notificadorFactory = new NotificadorFactory();
    
    // Patrón Observer - Notificadores
    public interface NotificadorObserver {
        void notificar(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo);
    }
    
    @Transactional
    public void enviarNotificacionBienvenida(Usuario usuario) {
        crearNotificacion(usuario, 
                         "¡Bienvenido a la plataforma!", 
                         "Tu cuenta ha sido creada exitosamente. ¡Comienza a buscar scrims!",
                         Notificacion.TipoNotificacion.NUEVA_POSTULACION);
        
        // Enviar email de bienvenida
        if (emailEnabled) {
            emailService.enviarEmailBienvenida(usuario);
        }
    }
    
    @Transactional
    public void notificarPostulacionAceptada(Usuario usuario, String nombreScrim) {
        crearNotificacion(usuario,
                         "Postulación Aceptada",
                         "Tu postulación para el scrim '" + nombreScrim + "' ha sido aceptada",
                         Notificacion.TipoNotificacion.POSTULACION_ACEPTADA);
    }
    
    @Transactional
    public void notificarPostulacionRechazada(Usuario usuario, String nombreScrim) {
        crearNotificacion(usuario,
                         "Postulación Rechazada",
                         "Tu postulación para el scrim '" + nombreScrim + "' ha sido rechazada",
                         Notificacion.TipoNotificacion.POSTULACION_RECHAZADA);
    }
    
    @Transactional
    public void notificarNuevaPostulacion(Usuario creadorScrim, String nombreUsuario, String nombreScrim) {
        crearNotificacion(creadorScrim,
                         "Nueva Postulación",
                         "El usuario " + nombreUsuario + " se ha postulado para tu scrim '" + nombreScrim + "'",
                         Notificacion.TipoNotificacion.NUEVA_POSTULACION);
    }
    
    @Transactional
    public void notificarEquipoCompleto(Usuario usuario, String nombreScrim) {
        crearNotificacion(usuario,
                         "Equipo Completo",
                         "El equipo para el scrim '" + nombreScrim + "' está completo. ¡Prepárate para jugar!",
                         Notificacion.TipoNotificacion.EQUIPO_COMPLETO);
    }
    
    @Transactional
    public void notificarScrimCancelado(Usuario usuario, String nombreScrim, String motivo) {
        crearNotificacion(usuario,
                         "Scrim Cancelado",
                         "El scrim '" + nombreScrim + "' ha sido cancelado. Motivo: " + motivo,
                         Notificacion.TipoNotificacion.SCRIM_CANCELADO);
    }
    
    @Transactional(readOnly = true)
    public List<Notificacion> obtenerNotificacionesUsuario(Usuario usuario) {
        return notificacionRepository.findByUsuarioOrderByFechaCreacionDesc(usuario);
    }
    
    @Transactional(readOnly = true)
    public List<Notificacion> obtenerNotificacionesNoLeidas(Usuario usuario) {
        return notificacionRepository.findNotificacionesNoLeidasPorUsuario(usuario);
    }
    
    @Transactional(readOnly = true)
    public Long contarNotificacionesNoLeidas(Usuario usuario) {
        return notificacionRepository.countNotificacionesNoLeidasPorUsuario(usuario);
    }
    
    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        notificacionRepository.findById(notificacionId).ifPresent(notificacion -> {
            notificacion.marcarComoLeida();
            notificacionRepository.save(notificacion);
        });
    }
    
    @Transactional
    public void marcarTodasComoLeidas(Usuario usuario) {
        List<Notificacion> noLeidas = notificacionRepository.findByUsuarioAndLeidaFalse(usuario);
        noLeidas.forEach(notificacion -> {
            notificacion.marcarComoLeida();
            notificacionRepository.save(notificacion);
        });
    }
    
    @Transactional
    public void crearNotificacion(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
        Notificacion notificacion = Notificacion.builder()
                .usuario(usuario)
                .titulo(titulo)
                .mensaje(mensaje)
                .tipo(tipo)
                .build();
        
        notificacionRepository.save(notificacion);
        
        // Aquí se podría implementar el patrón Observer para notificar por email, push, Discord, etc.
        notificarObservers(usuario, titulo, mensaje, tipo);
    }
    
    private void notificarObservers(Usuario usuario, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
        // Patrón Observer + Factory INTEGRADOS
        
        // Usar factory para crear todos los notificadores disponibles
        List<Notificador> notificadores = notificadorFactory.crearTodosLosNotificadores();
        
        // Observer pattern: notificar a todos los observadores disponibles
        notificadores.forEach(notificador -> {
            if (notificador.isDisponible()) {
                try {
                    notificador.enviar(usuario, titulo, mensaje, tipo);
                } catch (Exception e) {
                    System.err.println("Error enviando " + notificador.getTipo() + 
                                     " a " + usuario.getUsername() + ": " + e.getMessage());
                }
            }
        });
    }
    
    // Método público para enviar con notificador específico
    public void enviarConTipoEspecifico(Usuario usuario, String titulo, String mensaje, 
                                       Notificacion.TipoNotificacion tipo, String tipoNotificador) {
        try {
            Notificador notificador = notificadorFactory.crearNotificador(tipoNotificador);
            if (notificador.isDisponible()) {
                notificador.enviar(usuario, titulo, mensaje, tipo);
            }
        } catch (Exception e) {
            System.err.println("Error enviando " + tipoNotificador + ": " + e.getMessage());
        }
    }
}