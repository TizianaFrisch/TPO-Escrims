package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.states;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.ScrimState;

public class ConfirmadoState implements ScrimState {
    @Override public ScrimEstado getType() { return ScrimEstado.CONFIRMADO; }
    @Override public boolean canTransitionTo(ScrimEstado target) {
        return switch (target) {
            case EN_JUEGO, CANCELADO -> true;
            default -> false;
        };
    }
}
