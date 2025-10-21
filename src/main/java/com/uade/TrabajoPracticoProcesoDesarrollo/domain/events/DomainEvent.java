package com.uade.TrabajoPracticoProcesoDesarrollo.domain.events;

public interface DomainEvent {}


public record ScrimCreatedEvent(Long scrimId) implements DomainEvent { }
public record ScrimStateChangedEvent(Long scrimId, String nuevoEstado) implements DomainEvent { }
public record LobbyCompletedEvent(Long scrimId) implements DomainEvent { }
