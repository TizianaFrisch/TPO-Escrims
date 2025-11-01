package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Scrim;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;

/**
 * Explicit Context for the State pattern around Scrim lifecycle.
 * It wraps a Scrim aggregate and delegates transition rules to ScrimState.
 */
public class ScrimContext {
    private final Scrim scrim;
    private ScrimState state;

    public ScrimContext(Scrim scrim) {
        this.scrim = scrim;
        this.state = ScrimStateFactory.of(scrim.getEstado());
    }

    public Scrim getScrim() { return scrim; }

    public ScrimEstado getEstado() { return scrim.getEstado(); }

    public boolean canTransitionTo(ScrimEstado target) {
        return state.canTransitionTo(target);
    }

    public void transitionTo(ScrimEstado target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateException("Transicion invalida de " + scrim.getEstado() + " a " + target);
        }
        scrim.setEstado(target);
        this.state = ScrimStateFactory.of(target);
    }
}
