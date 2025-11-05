package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.ScrimEstado;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.states.*;

public final class ScrimStateFactory {
    private ScrimStateFactory(){}
    public static ScrimState of(ScrimEstado estado){
        return switch (estado){
            case BUSCANDO -> new BuscandoState();
            case LOBBY_ARMADO -> new LobbyArmadoState();
            case CONFIRMADO -> new ConfirmadoState();
            case EN_JUEGO -> new EnJuegoState();
            case FINALIZADO -> new FinalizadoState();
            case CANCELADO -> new CanceladoState();
        };
    }
}
