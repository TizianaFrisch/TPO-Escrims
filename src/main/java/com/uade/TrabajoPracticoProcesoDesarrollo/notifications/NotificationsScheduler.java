package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ConfirmacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.PostulacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.TipoNotificacion;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationsScheduler {
    private final ScrimRepository scrimRepo;
    private final ConfirmacionRepository confirmacionRepo;
    private final PostulacionRepository postulacionRepo;
    private final NotificacionService notificacionService;

    // Hours before the scrim to send reminders
    @Value("${app.notifications.reminderHours:4}")
    private int reminderHours;

    // Window minutes around the target hour to include scrims
    @Value("${app.notifications.reminderWindowMinutes:60}")
    private int windowMinutes;

    // Interval to run scheduler (ms)
    @Value("${app.notifications.reminderIntervalMs:60000}")
    private long intervalMs;

    // In-memory cache to avoid duplicate reminders during app lifetime
    private final Set<Long> reminded = ConcurrentHashMap.newKeySet();

    public NotificationsScheduler(ScrimRepository scrimRepo,
                                  ConfirmacionRepository confirmacionRepo,
                                  PostulacionRepository postulacionRepo,
                                  NotificacionService notificacionService) {
        this.scrimRepo = scrimRepo;
        this.confirmacionRepo = confirmacionRepo;
        this.postulacionRepo = postulacionRepo;
        this.notificacionService = notificacionService;
    }

    @Scheduled(fixedDelayString = "${app.notifications.reminderIntervalMs:60000}")
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetStart = now.plusHours(reminderHours);
        LocalDateTime targetEnd = targetStart.plusMinutes(windowMinutes);

        var scrims = scrimRepo.findAll();
        for (var s : scrims) {
            if (s.getFechaHora() == null) continue;
            if (s.getFechaHora().isBefore(targetStart) || s.getFechaHora().isAfter(targetEnd)) continue;
            Long scrimId = s.getId();
            if (scrimId == null) continue;
            if (reminded.contains(scrimId)) continue;

            // Prepare recipients: confirmed users first, otherwise accepted postulaciones
            var confirmaciones = confirmacionRepo.findByScrimId(scrimId);
            boolean sent = false;
            for (var c : confirmaciones) {
                if (c.isConfirmado() && c.getUsuario() != null && c.getUsuario().getId() != null) {
                    notificacionService.crearYEnviarATodosCanales(c.getUsuario().getId(), "Recordatorio: Scrim próximamente", "Tu scrim #" + scrimId + " inicia en aproximadamente " + reminderHours + " horas.", TipoNotificacion.LOBBY_ARMADO);
                    sent = true;
                }
            }
            if (!sent) {
                var aceptadas = postulacionRepo.findByScrimId(scrimId).stream().filter(p -> p.getEstado() != null && p.getEstado().name().equals("ACEPTADA")).toList();
                for (var p : aceptadas) {
                    if (p.getUsuario() != null && p.getUsuario().getId() != null) {
                        notificacionService.crearYEnviarATodosCanales(p.getUsuario().getId(), "Recordatorio: Scrim próximamente", "Fuiste aceptado en el scrim #" + scrimId + ". Inicia en ~" + reminderHours + " horas.", TipoNotificacion.LOBBY_ARMADO);
                    }
                }
            }

            // Mark as reminded for this app run
            reminded.add(scrimId);
        }
    }

    /**
     * Manual trigger for running the reminder job (useful for tests or admin actions).
     */
    public void triggerNow() {
        sendReminders();
    }

    /**
     * Expose current in-memory reminded set (snapshot).
     */
    public java.util.Set<Long> getReminded() {
        return java.util.Set.copyOf(reminded);
    }

    public int getReminderHours() { return reminderHours; }
    public int getWindowMinutes() { return windowMinutes; }
    public long getIntervalMs() { return intervalMs; }
}
