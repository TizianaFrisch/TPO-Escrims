package com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands;

import org.springframework.stereotype.Component;

@Component
public class InvitarJugadorCommand implements ScrimCommand {
    private final String nombre = "InvitarJugador";

    @Override
    public String name() { return nombre; }

    @Override
    public Object execute(Long scrimId, Long actorId, String payload) {
        // Simula invitación: devuelve success + payload
        return java.util.Map.of("scrimId", scrimId, "invitedBy", actorId, "payload", payload, "invited", true);
    }
}
