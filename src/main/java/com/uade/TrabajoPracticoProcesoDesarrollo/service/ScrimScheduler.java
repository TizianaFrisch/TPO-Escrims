package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ScrimScheduler {
    private static final Logger log = LoggerFactory.getLogger(ScrimScheduler.class);
    private final ScrimRepository scrimRepo;
    private final ScrimService scrimService;

    public ScrimScheduler(ScrimRepository scrimRepo, ScrimService scrimService, MatchmakingService matchmakingService) {
        this.scrimRepo = scrimRepo;
        this.scrimService = scrimService;
    }

    // Run every minute
    @Scheduled(fixedDelayString = "60000")
    public void startConfirmedScrims() {
        List<com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim> list = scrimRepo.findByEstado(com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado.CONFIRMADO);
        var now = LocalDateTime.now();
        for (var s : list) {
            if (s.getFechaHora() != null && !s.getFechaHora().isAfter(now)) {
                try {
                    scrimService.iniciar(s.getId());
                    log.info("Auto-iniciado scrim {}", s.getId());
                } catch (Exception ex) {
                    log.warn("No se pudo auto-iniciar scrim {}: {}", s.getId(), ex.getMessage());
                }
            }
        }
    }

    // Auto-matchmaking: cada 5s intenta armar lobby para scrims en BUSCANDO
    @Scheduled(fixedDelayString = "5000")
    public void autoMatchmaking() {
        var buscandos = scrimRepo.findByEstado(com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado.BUSCANDO);
        for (var s : buscandos) {
            try {
                // Ejecuta estrategia MMR sobre pendientes; si hay >=2 aceptadas, ScrimService cambiará a LOBBY_ARMADO
                scrimService.runMatchmaking(s.getId(), "mmr");
            } catch (Exception ex) {
                log.debug("Auto-matchmaking falló para scrim {}: {}", s.getId(), ex.getMessage());
            }
        }
    }
}
