package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ConfirmacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationsScheduler {
    private static final Logger log = LoggerFactory.getLogger(NotificationsScheduler.class);

    private final ScrimRepository scrimRepo;
    private final ConfirmacionRepository confirmacionRepo;
    private final NotificacionService notificacionService;

    @Value("${app.notifications.reminder.minutesBefore:60}")
    private int minutesBefore;

    public NotificationsScheduler(ScrimRepository scrimRepo,
                                  ConfirmacionRepository confirmacionRepo,
                                  NotificacionService notificacionService) {
        this.scrimRepo = scrimRepo;
        this.confirmacionRepo = confirmacionRepo;
        this.notificacionService = notificacionService;
    }

    // Revisa cada minuto si hay scrims confirmados próximos y envía recordatorio a confirmados
    @Scheduled(fixedDelayString = "60000")
    public void remindUpcomingScrims() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now;
        LocalDateTime windowEnd = now.plusMinutes(Math.max(5, minutesBefore));

        List<Scrim> confirmados = scrimRepo.findByEstado(ScrimEstado.CONFIRMADO);
        for (Scrim s : confirmados) {
            LocalDateTime fecha = s.getFechaHora();
            if (fecha == null) continue;
            if (!fecha.isBefore(windowStart) && !fecha.isAfter(windowEnd)) {
                var confs = confirmacionRepo.findByScrimId(s.getId());
                for (var c : confs) {
                    if (!c.isConfirmado()) continue;
                    try {
                        notificacionService.crearYEnviarATodosCanales(
                                c.getUsuario().getId(),
                                "Recordatorio de scrim",
                                "Tu scrim "+s.getId()+" empieza en ~" + Duration.between(now, fecha).toMinutes() + " min.",
                                com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.TipoNotificacion.CONFIRMADO
                        );
                    } catch (Exception ex) {
                        log.debug("Fallo recordatorio para usuario {} en scrim {}: {}", c.getUsuario().getId(), s.getId(), ex.getMessage());
                    }
                }
            }
        }
    }
}
