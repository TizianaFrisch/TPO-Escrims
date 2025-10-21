@Component
public class ScrimCreatedSubscriber implements Subscriber {
    private final NotificationService notifier;
    // inyectá lo que necesites para armar Notificacion: repos de Scrim/Usuario, etc.

    public ScrimCreatedSubscriber(NotificationService notifier) { this.notifier = notifier; }

    @Override
    public void onEvent(DomainEvent e) {
        if (!(e instanceof ScrimCreatedEvent ev)) return;
        // armar payload/resumen/destinos (ej: organizador, interesados por búsquedas favoritas)
        Notificacion n = Notificacion.builder()
                .tipo("Scrim creado")
                .payloadResumen("Nuevo scrim creado #" + ev.scrimId())
                // setDestinoEmail / setDestinoPushToken / etc, si corresponde
                .build();
        notifier.notifyAllChannels(n);
    }
}
