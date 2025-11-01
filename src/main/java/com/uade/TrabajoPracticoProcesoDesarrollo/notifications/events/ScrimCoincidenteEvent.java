package com.uade.TrabajoPracticoProcesoDesarrollo.notifications.events;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.DomainEvent;

public class ScrimCoincidenteEvent implements DomainEvent {
	private final Long usuarioId;
	private final Long scrimId;
	public ScrimCoincidenteEvent(Long usuarioId, Long scrimId) {
		this.usuarioId = usuarioId;
		this.scrimId = scrimId;
	}
	public Long usuarioId() { return usuarioId; }
	public Long scrimId() { return scrimId; }
}
