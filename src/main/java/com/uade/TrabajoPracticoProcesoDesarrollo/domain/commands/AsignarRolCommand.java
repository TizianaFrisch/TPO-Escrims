package com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands;

import org.springframework.stereotype.Component;

/**
 * Implementación mínima de un comando: AsignarRolCommand.
 * Para demo, su execute devuelve un map con resultado simple.
 */
@Component
public class AsignarRolCommand implements ScrimCommand {
    private final String nombre = "AsignarRol";

    @Override
    public String name() { return nombre; }

    @Override
    public Object execute(Long scrimId, Long actorId, String payload) {
        // payload podría contener JSON con detalles; por ahora devolvemos un resultado simple
        return java.util.Map.of("scrimId", scrimId, "actorId", actorId, "payload", payload, "ok", true);
    }
}
