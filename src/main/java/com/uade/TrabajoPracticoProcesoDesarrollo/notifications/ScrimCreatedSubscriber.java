package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimCreatedEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.UsuarioRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.TipoNotificacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.BusquedaFavoritaService;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScrimCreatedSubscriber {
    private static final Logger log = LoggerFactory.getLogger(ScrimCreatedSubscriber.class);

    private final DomainEventBus bus;
    private final NotificacionService notificacionService;
    private final ScrimRepository scrimRepository;
    private final UsuarioRepository usuarioRepository;
    private final BusquedaFavoritaService busquedaFavoritaService;

    @Value("${app.notifications.scrim.matchLimit:5}")
    private int matchLimit;

    public ScrimCreatedSubscriber(
        DomainEventBus bus,
        NotificacionService notificacionService,
        ScrimRepository scrimRepository,
        UsuarioRepository usuarioRepository,
        BusquedaFavoritaService busquedaFavoritaService
    ) {
        this.bus = bus;
        this.notificacionService = notificacionService;
        this.scrimRepository = scrimRepository;
        this.usuarioRepository = usuarioRepository;
        this.busquedaFavoritaService = busquedaFavoritaService;
    }

    @PostConstruct
    public void subscribe() {
        bus.subscribe(this::onEvent);
    }

    private void onEvent(com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.DomainEvent e) {
        if (!(e instanceof ScrimCreatedEvent)) return;
        ScrimCreatedEvent ev = (ScrimCreatedEvent) e;
        String titulo = "Scrim creado";
        try {
            Scrim s = scrimRepository.findById(ev.scrimId()).orElse(null);
            if (s != null && s.getCreador() != null) {
                Usuario creador = s.getCreador();
                String mensaje = "Tu scrim fue creado exitosamente. Estado: BUSCANDO.";
                // Notificar al creador
                notificacionService.crearYEnviarATodosCanales(creador.getId(), titulo, mensaje, TipoNotificacion.SCRIM_CREADO);
                log.info("ScrimCreatedSubscriber notified creator (id={}) for scrimId={}", creador.getId(), ev.scrimId());

                // Buscar candidatos: regla simple = usuarios con al menos una notificación activada (se puede extender)
                List<Usuario> candidatos = usuarioRepository.findAll().stream()
                        .filter(u -> u != null && !u.getId().equals(creador.getId()))
                        .filter(u -> Boolean.TRUE.equals(u.getNotifyPush()) || Boolean.TRUE.equals(u.getNotifyEmail()) || Boolean.TRUE.equals(u.getNotifyDiscord()))
                        // Preferencias de juego
                        .filter(u -> u.getJuegoPrincipal() != null && s.getJuego() != null && u.getJuegoPrincipal().getId().equals(s.getJuego().getId()))
                        // Preferencias de región
                        .filter(u -> u.getRegion() != null && s.getRegion() != null && u.getRegion().equalsIgnoreCase(s.getRegion()))
                        // Preferencias de rango (MMR)
                        .filter(u -> u.getMmr() != null && s.getRangoMin() != null && s.getRangoMax() != null
                                && u.getMmr() >= s.getRangoMin() && u.getMmr() <= s.getRangoMax())
                        // Preferencias de latencia
                        .filter(u -> u.getLatencia() != null && s.getLatenciaMax() != null && u.getLatencia() <= s.getLatenciaMax())
                        .limit(matchLimit)
                        .collect(Collectors.toList());

                log.info("ScrimCreated: scrimId={} -> notifying {} candidate(s) (limit={})", ev.scrimId(), candidatos.size(), matchLimit);

                for (Usuario u : candidatos) {
                    try {
                        String userMsg = String.format("Nuevo scrim disponible: #%d - %s", ev.scrimId(), s.getNombre() != null ? s.getNombre() : "");
                        notificacionService.crearYEnviarATodosCanales(u.getId(), titulo, userMsg, TipoNotificacion.SCRIM_CREADO);
                        log.debug("Notificación creada/enviada para usuario id={}", u.getId());
                    } catch (Exception ex) {
                        log.error("Error al notificar usuario id={}: {}", u != null ? u.getId() : null, ex.getMessage(), ex);
                    }
                }

                // Notificación por búsquedas guardadas
                String juego = s.getJuego() != null ? s.getJuego().getNombre() : null;
                String region = s.getRegion();
                Integer rango = s.getRangoMin(); // o el rango que corresponda
                Integer latencia = s.getLatenciaMax();
                busquedaFavoritaService.notificarCoincidentes(juego, region, rango, latencia, s.getId());
            } else {
                log.info("ScrimCreatedSubscriber: scrim {} sin creador asociado, no se envía notificación específica", ev.scrimId());
            }
        } catch (Exception ex) {
            log.error("Error processing ScrimCreatedEvent: {}", ex.getMessage(), ex);
        }
    }
}

