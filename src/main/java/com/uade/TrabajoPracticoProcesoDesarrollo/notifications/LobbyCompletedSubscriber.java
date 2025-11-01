package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Subscriber;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.LobbyCompletedEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import org.springframework.stereotype.Component;

@Component
public class LobbyCompletedSubscriber implements Subscriber {
    private final NotificacionService notifier;

    public LobbyCompletedSubscriber(NotificacionService notifier) { this.notifier = notifier; }

    @Override
    public void onEvent(Object e) {
        if (!(e instanceof LobbyCompletedEvent ev)) return;
        notifier.crearYEnviarATodosCanales(0L, "Lobby armado", "Tu scrim #" + ev.scrimId() + " completó el cupo.", null);
    }
}
