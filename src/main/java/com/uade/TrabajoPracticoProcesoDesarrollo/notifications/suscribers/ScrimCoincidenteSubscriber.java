package com.uade.escrims.notifications.subscribers;

import com.uade.escrims.notifications.Notificacion;
import com.uade.escrims.notifications.Subscriber;
import com.uade.escrims.notifications.events.DomainEvent;
import com.uade.escrims.notifications.events.ScrimCoincidenteEvent;
import com.uade.escrims.notifications.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class ScrimCoincidenteSubscriber implements Subscriber {
    private final NotificationService notifier;
    public ScrimCoincidenteSubscriber(NotificationService notifier){ this.notifier = notifier; }

    @Override public void onEvent(DomainEvent e) {
        if (!(e instanceof ScrimCoincidenteEvent ev)) return;
        var n = Notificacion.builder()
                .tipo("Scrim coincidente")
                .payloadResumen("Te matcheó el scrim #" + ev.scrimId())
                .build();
        notifier.notifyAllChannels(n);
    }
}
