package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.states;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.ScrimState;

public class EnJuegoState implements ScrimState {
    @Override public ScrimEstado getType() { return ScrimEstado.EN_JUEGO; }
    @Override public boolean canTransitionTo(ScrimEstado target) {
        return target == ScrimEstado.FINALIZADO;
    }
}
