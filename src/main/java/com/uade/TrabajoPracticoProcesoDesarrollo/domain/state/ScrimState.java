package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;

public interface ScrimState {
    ScrimEstado getType();
    boolean canTransitionTo(ScrimEstado target);
}
