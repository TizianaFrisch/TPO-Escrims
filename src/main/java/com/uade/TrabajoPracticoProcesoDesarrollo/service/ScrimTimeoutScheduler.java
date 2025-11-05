package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ConfirmacionRepository;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class ScrimTimeoutScheduler {

    private final ScrimRepository scrimRepository;
    private final ConfirmacionRepository confirmacionRepository;
    private final ScrimService scrimService;
    private final AuditLogService auditLogService;

    @Value("${scrim.timeout.minutes:30}")
    private int timeoutMinutes;

    public ScrimTimeoutScheduler(ScrimRepository scrimRepository,
                                 ConfirmacionRepository confirmacionRepository,
                                 ScrimService scrimService,
                                 AuditLogService auditLogService) {
        this.scrimRepository = scrimRepository;
        this.confirmacionRepository = confirmacionRepository;
        this.scrimService = scrimService;
        this.auditLogService = auditLogService;
    }

    // Cancela scrims que no alcanzaron el cupo dentro del tiempo desde su creación
    @Scheduled(fixedDelayString = "${scrim.timeout.check-ms:60000}")
    @Transactional
    public void cancelTimedOutScrims() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeoutMinutes);
        var estados = List.of(ScrimEstado.BUSCANDO, ScrimEstado.LOBBY_ARMADO, ScrimEstado.CONFIRMADO);
        var candidatos = scrimRepository.findByEstadoInAndFechaCreacionBefore(estados, cutoff);
        for (Scrim s : candidatos) {
            Integer cupos = s.getCuposTotal();
            int requeridos = cupos != null ? cupos : 0;
            long confirmados = confirmacionRepository.countByScrimIdAndConfirmado(s.getId(), true);
            if (confirmados < requeridos) {
                try { scrimService.cancelar(s.getId()); } catch (Exception ignore) {}
                try {
                    auditLogService.log(
                            "Scrim",
                            s.getId(),
                            "CANCELAR_POR_TIMEOUT",
                            "system",
                            Map.of(
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
