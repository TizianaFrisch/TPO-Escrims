package com.uade.TrabajoPracticoProcesoDesarrollo.notifications.suscribers;

import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.events.ScrimCoincidenteEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.TipoNotificacion;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Subscriber;
import org.springframework.stereotype.Component;
// import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.service.NotificationService;

@Component
public class ScrimCoincidenteSubscriber implements Subscriber {
    private final NotificacionService notifier;
    public ScrimCoincidenteSubscriber(NotificacionService notifier){ this.notifier = notifier; }

    @Override
    public void onEvent(Object e) {
        if (!(e instanceof ScrimCoincidenteEvent)) return;
        ScrimCoincidenteEvent ev = (ScrimCoincidenteEvent) e;
    // Usa el tipo SCRIM_CREADO por falta de SCRIM_COINCIDENTE en el enum
    notifier.crearYEnviarATodosCanales(ev.usuarioId(), "Scrim Coincidente", "Te matcheó el scrim #" + ev.scrimId(), TipoNotificacion.SCRIM_CREADO);
    }
}
