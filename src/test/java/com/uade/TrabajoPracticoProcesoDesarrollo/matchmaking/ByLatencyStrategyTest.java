package com.uade.TrabajoPracticoProcesoDesarrollo.matchmaking;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.VerificacionEstado;

class ByLatencyStrategyTest {
    @Test
    void seleccionaPorLatenciaMaximaYOrdenaAscendente() {
        Usuario u1 = new Usuario(); u1.setId(1L); u1.setLatencia(50); u1.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        Usuario u2 = new Usuario(); u2.setId(2L); u2.setLatencia(100); u2.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        Usuario u3 = new Usuario(); u3.setId(3L); u3.setLatencia(30); u3.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        Usuario u4 = new Usuario(); u4.setId(4L); u4.setLatencia(200); u4.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        List<Usuario> candidatos = Arrays.asList(u1, u2, u3, u4);
        Scrim scrim = new Scrim();
        scrim.setLatenciaMax(100);
        scrim.setCuposTotal(3);
        ByLatencyStrategy strategy = new ByLatencyStrategy();
        List<Usuario> seleccionados = strategy.seleccionar(candidatos, scrim);
        assertEquals(3, seleccionados.size());
        assertEquals(u3, seleccionados.get(0)); // menor latencia primero
        assertEquals(u1, seleccionados.get(1));
        assertEquals(u2, seleccionados.get(2));
    }

    @Test
    void ignoraUsuariosSinLatencia() {
        Usuario u1 = new Usuario(); u1.setId(1L); u1.setLatencia(null); u1.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        Usuario u2 = new Usuario(); u2.setId(2L); u2.setLatencia(80); u2.setVerificacionEstado(VerificacionEstado.PENDIENTE);
        Scrim scrim = new Scrim();
        scrim.setLatenciaMax(100);
        ByLatencyStrategy strategy = new ByLatencyStrategy();
        List<Usuario> seleccionados = strategy.seleccionar(Arrays.asList(u1, u2), scrim);
        assertEquals(1, seleccionados.size());
        assertEquals(u2, seleccionados.get(0));
    }
}
