package com.uade.TrabajoPracticoProcesoDesarrollo.domain.events;

import java.time.Instant;

public record ScrimStateChanged(Long scrimId, String anteriorEstado, String nuevoEstado, Instant timestamp) implements DomainEvent {}
