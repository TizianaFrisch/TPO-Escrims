@Component
public class ScrimStateChangedSubscriber implements Subscriber {
    private final NotificationService notifier;

    public ScrimStateChangedSubscriber(NotificationService notifier) { this.notifier = notifier; }

    @Override
    public void onEvent(DomainEvent e) {
        if (!(e instanceof ScrimStateChangedEvent ev)) return;
        var resumen = "Scrim #" + ev.scrimId() + " → " + ev.nuevoEstado();
        Notificacion n = Notificacion.builder()
                .tipo("Cambio de estado")
                .payloadResumen(resumen)
                .build();
        notifier.notifyAllChannels(n);
    }
}
