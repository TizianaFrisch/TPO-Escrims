package com.uade.TrabajoPracticoProcesoDesarrollo.domain.state.states;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Subscriber;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimStateChangedEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import org.springframework.stereotype.Component;

@Component
public class ScrimStateChangedSubscriber implements Subscriber {
    private final NotificacionService notifier;

    public ScrimStateChangedSubscriber(NotificacionService notifier) { this.notifier = notifier; }

    @Override
    public void onEvent(Object e) {
        if (!(e instanceof ScrimStateChangedEvent ev)) return;
        var resumen = "Scrim #" + ev.scrimId() + " → " + ev.nuevoEstado();
        // Crear y enviar via NotificacionService (usa stubs según configuración)
        notifier.crearYEnviarATodosCanales(0L, "Cambio de estado", resumen, null);
    }
}
