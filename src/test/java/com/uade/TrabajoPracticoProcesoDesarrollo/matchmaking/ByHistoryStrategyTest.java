package com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;

class ByHistoryStrategyTest {
    @Test
    void seleccionaPorMayorIdSimulandoHistorial() {
        Usuario u1 = new Usuario(); u1.setId(1L); u1.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        Usuario u2 = new Usuario(); u2.setId(5L); u2.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        Usuario u3 = new Usuario(); u3.setId(3L); u3.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        List<Usuario> candidatos = Arrays.asList(u1, u2, u3);
        Scrim scrim = new Scrim();
        scrim.setCuposTotal(2);
        ByHistoryStrategy strategy = new ByHistoryStrategy();
        List<Usuario> seleccionados = strategy.seleccionar(candidatos, scrim);
        assertEquals(2, seleccionados.size());
        assertEquals(u2, seleccionados.get(0)); // mayor id primero
        assertEquals(u3, seleccionados.get(1));
    }

    @Test
    void seleccionaTodosSiNoHayLimite() {
        Usuario u1 = new Usuario(); u1.setId(2L); u1.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        Usuario u2 = new Usuario(); u2.setId(1L); u2.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        List<Usuario> candidatos = Arrays.asList(u1, u2);
        Scrim scrim = new Scrim();
        scrim.setCuposTotal(null);
        ByHistoryStrategy strategy = new ByHistoryStrategy();
        List<Usuario> seleccionados = strategy.seleccionar(candidatos, scrim);
        assertEquals(2, seleccionados.size());
        assertEquals(u1, seleccionados.get(0));
        assertEquals(u2, seleccionados.get(1));
    }
}
