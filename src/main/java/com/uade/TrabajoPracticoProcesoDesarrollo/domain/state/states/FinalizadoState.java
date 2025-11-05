package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.states;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.ScrimState;

public class FinalizadoState implements ScrimState {
    @Override public ScrimEstado getType() { return ScrimEstado.FINALIZADO; }
    @Override public boolean canTransitionTo(ScrimEstado target) { return false; }
}
