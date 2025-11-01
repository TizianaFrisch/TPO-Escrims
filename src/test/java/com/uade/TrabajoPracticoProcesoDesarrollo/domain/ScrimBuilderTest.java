package com.uade.TrabajoPracticoProcesoDesarrollo.domain;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.builders.ScrimBuilder;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Juego;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ScrimBuilderTest {
    @Test
    void buildHappyPath(){
        Juego j = new Juego(); j.setId(1L); j.setNombre("Valorant");
        Scrim s = new ScrimBuilder()
                .juego(j)
                .region("LATAM")
                .formato("5v5")
                .rango(100,300)
                .latencia(80)
                .fechaHora(LocalDateTime.now().plusDays(1))
                .duracion(45)
                .cupos(10)
                .build();
        assertNotNull(s);
        assertEquals("LATAM", s.getRegion());
        assertEquals(10, s.getCuposTotal());
    }
}
