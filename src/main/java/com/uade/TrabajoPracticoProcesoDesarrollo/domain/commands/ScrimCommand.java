package com.uade.TrabajoPracticoProcesoDesarrollo.domain.commands;

/**
 * Interfaz mínima para comandos de Scrim (Command pattern).
 * Implementaciones pueden proporcionar `name()` para registro y `execute` para la acción.
 */
public interface ScrimCommand {
    String name();
    Object execute(Long scrimId, Long actorId, String payload);
}
