package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.DomainEvent;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.ScrimStateChanged;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationSubscriber {
    private static final Logger log = LoggerFactory.getLogger(NotificationSubscriber.class);
    private final DomainEventBus bus;
    private final NotifierFactory notifierFactory;

    public NotificationSubscriber(DomainEventBus bus, NotifierFactory notifierFactory) {
        this.bus = bus;
        this.notifierFactory = notifierFactory;
    }

    @PostConstruct
    void subscribe(){
        bus.subscribe(this::onEvent);
    }

    private void onEvent(DomainEvent event){
        if (event instanceof ScrimStateChanged e) {
            var push = notifierFactory.createPush();
            String msg = String.format("Scrim #%d: %s -> %s at %s", e.scrimId(), e.anteriorEstado(), e.nuevoEstado(), e.timestamp());
            // demo: broadcast to console
            push.send("all", msg);
            log.info("Notification dispatched: {}", msg);
        }
    }
}
