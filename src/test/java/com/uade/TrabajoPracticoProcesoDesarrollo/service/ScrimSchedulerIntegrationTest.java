package com.uade.TrabajoPracticoProcesoDesarrollo.service;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.repository.ScrimRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@SuppressWarnings("null")
class ScrimSchedulerIntegrationTest {
    @Autowired ScrimRepository scrimRepo;
    @Autowired ScrimService scrimService;
    @Autowired ScrimScheduler scrimScheduler;

    @Test
    void startConfirmedScrims_inicia_los_vencidos() {
        // Crear un scrim en CONFIRMADO cuyo inicio ya pasó
        Scrim s = scrimRepo.findAll().stream().findFirst().orElse(null);
        if (s == null) return; // si no hay datos base, salteamos
        s.setEstado(ScrimEstado.CONFIRMADO);
        s.setFechaHora(LocalDateTime.now().minusMinutes(1));
        scrimRepo.save(s);

        // Ejecutar scheduler y verificar transición a EN_JUEGO
        scrimScheduler.startConfirmedScrims();
        Scrim reloaded = scrimRepo.findById(s.getId()).orElseThrow();
        assertEquals(ScrimEstado.EN_JUEGO, reloaded.getEstado());
    }
}
