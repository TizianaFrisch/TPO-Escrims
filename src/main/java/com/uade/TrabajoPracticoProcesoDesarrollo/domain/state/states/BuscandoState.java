package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.states;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.ScrimState;

public class BuscandoState implements ScrimState {
    @Override public ScrimEstado getType() { return ScrimEstado.BUSCANDO; }
    @Override public boolean canTransitionTo(ScrimEstado target) {
        return switch (target) {
            case LOBBY_ARMADO, CANCELADO -> true;
            default -> false;
        };
    }
}
