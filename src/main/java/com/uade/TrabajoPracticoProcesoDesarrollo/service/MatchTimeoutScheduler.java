package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Match;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ConfirmacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.MatchRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class MatchTimeoutScheduler {

    private final MatchRepository matchRepository;
    private final ConfirmacionRepository confirmacionRepository;
    private final ScrimService scrimService;
    private final ScrimRepository scrimRepository;
    private final AuditLogService auditLogService;

    @Value("${match.timeout.minutes:30}")
    private int timeoutMinutes;

    public MatchTimeoutScheduler(MatchRepository matchRepository,
                                 ConfirmacionRepository confirmacionRepository,
                                 ScrimService scrimService,
                                 AuditLogService auditLogService,
                                 ScrimRepository scrimRepository) {
        this.matchRepository = matchRepository;
        this.confirmacionRepository = confirmacionRepository;
        this.scrimService = scrimService;
        this.auditLogService = auditLogService;
        this.scrimRepository = scrimRepository;
    }

    // Corre cada 60 segundos por defecto; configurable vía match.timeout.check-ms
    @Scheduled(fixedDelayString = "${match.timeout.check-ms:60000}")
    @Transactional
    public void cancelTimedOutMatches() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        var pendientes = matchRepository.findByEstadoAndFechaInicioBefore(Match.EstadoMatch.EN_PROGRESO, cutoff);
        for (Match m : pendientes) {
            Scrim proxy = m.getScrim();
            Long scrimId = proxy != null ? proxy.getId() : null;
            if (scrimId == null) continue;
            // Cargar la entidad real para evitar LazyInitialization
            Scrim s = scrimRepository.findById(scrimId).orElse(null);
            if (s == null) continue;
            if (s.getEstado() == ScrimEstado.FINALIZADO || s.getEstado() == ScrimEstado.CANCELADO) continue;

            long confirmados = confirmacionRepository.countByScrimIdAndConfirmado(s.getId(), true);
            Integer cupos = s.getCuposTotal();
            int requeridos = cupos != null ? cupos : 0;

            // Si no alcanzó el cupo completo dentro del tiempo, cancelar automáticamente
            if (confirmados < requeridos) {
                try {
                    m.setEstado(Match.EstadoMatch.CANCELADO_POR_MODERADOR);
                    m.setFechaFin(LocalDateTime.now());
                    matchRepository.save(m);
                } catch (Exception ignore) {}

                // Cambia el estado del scrim a CANCELADO (y dispara auditoría desde el servicio)
                try { scrimService.cancelar(s.getId()); } catch (Exception ignore) {}

                // Auditoría específica del match timeout
                try {
                    auditLogService.log(
                            "Match",
                            m.getId(),
                            "CANCELAR_POR_TIMEOUT",
                            "system",
                            Map.of(
                                    "scrimId", s.getId(),
                                    "confirmados", confirmados,
                                    "requeridos", requeridos,
                                    "timeoutMin", timeoutMinutes
                            )
                    );
                } catch (Exception ignore) {}
            }
        }
    }
}
