package com.uade.escrims.notifications.events;
public record ScrimCoincidenteEvent(Long usuarioId, Long scrimId) implements DomainEvent {}
