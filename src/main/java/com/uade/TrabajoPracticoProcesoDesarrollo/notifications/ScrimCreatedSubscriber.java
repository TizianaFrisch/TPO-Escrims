package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimCreatedEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.BusquedaFavoritaService;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;


@Component
public class ScrimCreatedSubscriber {
    private static final Logger log = LoggerFactory.getLogger(ScrimCreatedSubscriber.class);

    private final DomainEventBus bus;
    private final NotificacionService notificacionService;
    private final ScrimRepository scrimRepository;
    private final BusquedaFavoritaService busquedaFavoritaService;

    public ScrimCreatedSubscriber(DomainEventBus bus, NotificacionService notificacionService, ScrimRepository scrimRepository,
                                  BusquedaFavoritaService busquedaFavoritaService) {
        this.bus = bus;
        this.notificacionService = notificacionService;
        this.scrimRepository = scrimRepository;
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
            Long id = java.util.Objects.requireNonNull(ev.scrimId());
            Scrim s = scrimRepository.findById(id).orElse(null);
            if (s != null && s.getCreador() != null) {
                Usuario creador = s.getCreador();
                String mensaje = "Tu scrim fue creado exitosamente. Estado: BUSCANDO.";
                // Enviar a través del servicio que respeta preferencias y canales
                notificacionService.crearYEnviarATodosCanales(creador.getId(), titulo, mensaje, null);
                log.info("ScrimCreatedSubscriber notified creator (id={}) for scrimId={}", creador.getId(), id);
            } else {
                log.info("ScrimCreatedSubscriber: scrim {} sin creador asociado, no se envía notificación específica", id);
            }

            // Además: notificar a usuarios con búsquedas favoritas coincidentes
            try {
                if (s != null && s.getJuego() != null) {
                    String juego = s.getJuego().getNombre();
                    String region = s.getRegion();
                    // Calcular un rango representativo (promedio si hay min/max, si no, usar el que exista; fallback 0)
                    Integer rangoProm = null;
                    if (s.getRangoMin() != null && s.getRangoMax() != null) {
                        rangoProm = (s.getRangoMin() + s.getRangoMax()) / 2;
                    } else if (s.getRangoMin() != null) {
                        rangoProm = s.getRangoMin();
                    } else if (s.getRangoMax() != null) {
                        rangoProm = s.getRangoMax();
                    } else {
                        rangoProm = 0;
                    }
                    Integer latencia = s.getLatenciaMax() != null ? s.getLatenciaMax() : 999;
                    busquedaFavoritaService.notificarCoincidentes(juego, region, rangoProm, latencia, id);
                    log.info("ScrimCreatedSubscriber dispatched favorite-search notifications for scrimId={} juego={} region={}", id, juego, region);
                }
            } catch (Exception ex2) {
                log.error("Error notifying favorite-search matches for scrim {}: {}", id, ex2.getMessage(), ex2);
            }
        } catch (Exception ex) {
            log.error("Error processing ScrimCreatedEvent: {}", ex.getMessage(), ex);
        }
    }
}
