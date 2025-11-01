package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class NotificationsConfig {
    private final DomainEventBus bus;
    private final ScrimCoincidenteSubscriber coincidente;
    public NotificationsConfig(DomainEventBus bus, ScrimCoincidenteSubscriber coincidente){
        this.bus = bus; this.coincidente = coincidente;
    }
    @PostConstruct
    public void subscribeAll() {
        bus.subscribe((java.util.function.Consumer) coincidente);
    }

    // Minimal local stub to replace missing external dependency:
    // If you have the real library, remove this stub and restore the original import.
    @Component
    public static class ScrimCoincidenteSubscriber implements java.util.function.Consumer<com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.DomainEvent> {
        public ScrimCoincidenteSubscriber() { }
        @Override
        public void accept(com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.DomainEvent domainEvent) {
            // no-op stub for local development. Implement behavior when dependency is available.
        }
    }
}
