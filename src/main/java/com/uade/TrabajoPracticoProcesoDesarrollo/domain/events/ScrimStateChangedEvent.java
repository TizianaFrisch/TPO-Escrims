package com.uade.TrabajoPracticoProcesoDesarrollo.domain.events;

public record ScrimStateChangedEvent(Long scrimId, String nuevoEstado) implements DomainEvent { }
