package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.states;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.ScrimState;

public class LobbyArmadoState implements ScrimState {
    @Override public ScrimEstado getType() { return ScrimEstado.LOBBY_ARMADO; }
    @Override public boolean canTransitionTo(ScrimEstado target) {
        return switch (target) {
            case CONFIRMADO, CANCELADO -> true;
            default -> false;
        };
    }
}
