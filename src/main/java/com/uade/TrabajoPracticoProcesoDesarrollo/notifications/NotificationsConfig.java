package com.uade.escrims.notifications;

import com.uade.escrims.notifications.subscribers.ScrimCoincidenteSubscriber;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationsConfig {
    private final DomainEventBus bus;
    private final ScrimCoincidenteSubscriber coincidente;
    public NotificationsConfig(DomainEventBus bus, ScrimCoincidenteSubscriber coincidente){
        this.bus = bus; this.coincidente = coincidente;
    }
    @PostConstruct public void subscribeAll(){ bus.subscribe(coincidente); }
}
