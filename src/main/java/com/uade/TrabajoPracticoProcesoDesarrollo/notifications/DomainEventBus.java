package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import com.uade.TrabajoPracticoProcesoDesarrollo.domain.events.DomainEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class DomainEventBus {
    private final List<Consumer<DomainEvent>> subscribers = new ArrayList<>();
    public void subscribe(Consumer<DomainEvent> sub){ subscribers.add(sub); }
    public void publish(DomainEvent e){ subscribers.forEach(s -> s.accept(e)); }
}
