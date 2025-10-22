package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import org.springframework.stereotype.Component;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimCreatedEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.service.NotificacionService;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Subscriber;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.enums.TipoNotificacion;

@Component
public class ScrimCreatedSubscriber implements Subscriber {
    private final NotificacionService notifier;
    // inyectá lo que necesites para armar Notificacion: repos de Scrim/Usuario, etc.

    public ScrimCreatedSubscriber(NotificacionService notifier) { this.notifier = notifier; }

    @Override
    public void onEvent(Object e) {
        if (!(e instanceof ScrimCreatedEvent)) return;
        ScrimCreatedEvent ev = (ScrimCreatedEvent) e;
        Long usuarioId = null; // Completa según tu lógica
        String titulo = "Scrim creado";
        String mensaje = "Nuevo scrim creado #" + ev.scrimId();
        notifier.crearYEnviarATodosCanales(usuarioId, titulo, mensaje, TipoNotificacion.SCRIM_CREADO);
    }
}
