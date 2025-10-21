package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.states.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScrimStateTransitionsTest {

    @Test
    void buscando_transitions() {
        ScrimState s = new BuscandoState();
        assertEquals(ScrimEstado.BUSCANDO, s.getType());
        assertTrue(s.canTransitionTo(ScrimEstado.LOBBY_ARMADO));
        assertTrue(s.canTransitionTo(ScrimEstado.CANCELADO));
        assertFalse(s.canTransitionTo(ScrimEstado.CONFIRMADO));
        assertFalse(s.canTransitionTo(ScrimEstado.EN_JUEGO));
    }

    @Test
    void lobbyArmado_transitions() {
        ScrimState s = new LobbyArmadoState();
        assertEquals(ScrimEstado.LOBBY_ARMADO, s.getType());
        assertTrue(s.canTransitionTo(ScrimEstado.CONFIRMADO));
        assertTrue(s.canTransitionTo(ScrimEstado.CANCELADO));
        assertFalse(s.canTransitionTo(ScrimEstado.EN_JUEGO));
    }

    @Test
    void confirmado_transitions() {
        ScrimState s = new ConfirmadoState();
        assertEquals(ScrimEstado.CONFIRMADO, s.getType());
        assertTrue(s.canTransitionTo(ScrimEstado.EN_JUEGO));
        assertTrue(s.canTransitionTo(ScrimEstado.CANCELADO));
        assertFalse(s.canTransitionTo(ScrimEstado.FINALIZADO));
    }

    @Test
    void enJuego_transitions() {
        ScrimState s = new EnJuegoState();
        assertEquals(ScrimEstado.EN_JUEGO, s.getType());
        assertTrue(s.canTransitionTo(ScrimEstado.FINALIZADO));
        assertFalse(s.canTransitionTo(ScrimEstado.CANCELADO));
    }

    @Test
    void finalizado_cancelado_no_transitions() {
        ScrimState f = new FinalizadoState();
        ScrimState c = new CanceladoState();
        assertEquals(ScrimEstado.FINALIZADO, f.getType());
        assertEquals(ScrimEstado.CANCELADO, c.getType());
        assertFalse(f.canTransitionTo(ScrimEstado.BUSCANDO));
        assertFalse(c.canTransitionTo(ScrimEstado.BUSCANDO));
    }
}
