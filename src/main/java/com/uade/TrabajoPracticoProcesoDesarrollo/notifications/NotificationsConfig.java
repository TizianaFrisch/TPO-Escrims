package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import java.util.function.Consumer;
import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.DomainEvent;

@Configuration
public class NotificationsConfig {
    private final DomainEventBus bus;
    private final ScrimCoincidenteSubscriber coincidente;
    public NotificationsConfig(DomainEventBus bus, ScrimCoincidenteSubscriber coincidente){
        this.bus = bus; this.coincidente = coincidente;
    }
    @PostConstruct
    public void subscribeAll() {
        bus.subscribe(coincidente);
    }

    // Minimal local stub to replace missing external dependency:
    // If you have the real library, remove this stub and restore the original import.
    @Component
    public static class ScrimCoincidenteSubscriber implements Consumer<DomainEvent> {
        public ScrimCoincidenteSubscriber() { }
        @Override
        public void accept(DomainEvent domainEvent) {
            // no-op stub for local development. Implement behavior when dependency is available.
        }
    }
}
