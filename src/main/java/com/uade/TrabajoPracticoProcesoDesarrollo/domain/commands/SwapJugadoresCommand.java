package com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands;

import org.springframework.stereotype.Component;

@Component
public class SwapJugadoresCommand implements ScrimCommand {
    private final String nombre = "SwapJugadores";

    @Override
    public String name() { return nombre; }

    @Override
    public Object execute(Long scrimId, Long actorId, String payload) {
        // Simula swap: devuelve success
        return java.util.Map.of("scrimId", scrimId, "actorId", actorId, "swapped", true, "payload", payload);
    }
}
