package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Postulacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.PostulacionEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.DomainEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimStateChanged;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.PostulacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DomainEventBus subscriber that sends real notifications (email/push/discord according to preferences)
 * on scrim state changes, using concise templates.
 */
@Component
public class ScrimStateChangedNotifier {
    private static final Logger log = LoggerFactory.getLogger(ScrimStateChangedNotifier.class);

    private final DomainEventBus bus;
    private final NotificacionService notificacionService;
    private final ScrimRepository scrimRepository;
    private final PostulacionRepository postulacionRepository;

    public ScrimStateChangedNotifier(DomainEventBus bus,
                                     NotificacionService notificacionService,
                                     ScrimRepository scrimRepository,
                                     PostulacionRepository postulacionRepository) {
        this.bus = bus;
        this.notificacionService = notificacionService;
        this.scrimRepository = scrimRepository;
        this.postulacionRepository = postulacionRepository;
    }

    @PostConstruct
    public void subscribe() { bus.subscribe(this::onEvent); }

    private void onEvent(DomainEvent e) {
        if (!(e instanceof ScrimStateChanged ev)) return;
        try {
            Scrim s = scrimRepository.findById(ev.scrimId()).orElse(null);
            String nuevo = ev.nuevoEstado() != null ? ev.nuevoEstado() : "";

            // Map to templates specified
            if (nuevo.equalsIgnoreCase(ScrimEstado.CONFIRMADO.name())) {
                notifyCreatorIfAny(s, "Scrim confirmado", "Tu scrim fue confirmado. Estado: CONFIRMADO.");
                notifyAcceptedParticipants(s, "Scrim confirmado", "El scrim al que te postulaste fue confirmado. Próximo paso: preparar inicio del juego.");
            } else if (nuevo.equalsIgnoreCase(ScrimEstado.FINALIZADO.name())) {
                notifyCreatorIfAny(s, "Scrim finalizado", "Tu scrim finalizó correctamente. ¡Gracias por participar!");
                notifyAcceptedParticipants(s, "Scrim finalizado", "El scrim en el que participaste finalizó. ¡Gracias por jugar!");
            } else if (nuevo.equalsIgnoreCase(ScrimEstado.CANCELADO.name())) {
                notifyCreatorIfAny(s, "Scrim cancelado", "Tu scrim fue cancelado por el organizador.");
                notifyAcceptedParticipants(s, "Scrim cancelado", "El scrim al que estabas aceptado fue cancelado por el organizador.");
            } else if (nuevo.equalsIgnoreCase(ScrimEstado.LOBBY_ARMADO.name())) {
                // Postulación aceptada → notify accepted postulants
                List<Postulacion> aceptadas = s != null ? postulacionRepository.findByScrimAndEstado(s, PostulacionEstado.ACEPTADA) : List.of();
                for (Postulacion p : aceptadas) {
                    Usuario u = p.getUsuario();
                    if (u != null) {
                        String msg = "Fuiste aceptado en el scrim " + ev.scrimId() + ". Estado actual: LOBBY_ARMADO.";
                        notificacionService.crearYEnviarATodosCanales(u.getId(), "Postulación aceptada", msg, null);
                    }
                }
            }
            log.info("ScrimStateChangedNotifier processed event {} -> {} for scrimId={}", ev.anteriorEstado(), ev.nuevoEstado(), ev.scrimId());
        } catch (Exception ex) {
            log.error("Error processing ScrimStateChanged: {}", ex.getMessage(), ex);
        }
    }

    private void notifyCreatorIfAny(Scrim s, String titulo, String mensaje) {
        if (s == null) return;
        Usuario creador = s.getCreador();
        if (creador != null) {
            notificacionService.crearYEnviarATodosCanales(creador.getId(), titulo, mensaje, null);
        }
    }

    private void notifyAcceptedParticipants(Scrim s, String titulo, String mensaje) {
        if (s == null) return;
        List<Postulacion> aceptadas = postulacionRepository.findByScrimAndEstado(s, PostulacionEstado.ACEPTADA);
        for (Postulacion p : aceptadas) {
            Usuario u = p.getUsuario();
            if (u != null) {
                notificacionService.crearYEnviarATodosCanales(u.getId(), titulo, mensaje, null);
            }
        }
    }
}
