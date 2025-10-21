@Component
public class LobbyCompletedSubscriber implements Subscriber {
    private final NotificationService notifier;

    public LobbyCompletedSubscriber(NotificationService notifier) { this.notifier = notifier; }

    @Override
    public void onEvent(DomainEvent e) {
        if (!(e instanceof LobbyCompletedEvent ev)) return;
        Notificacion n = Notificacion.builder()
                .tipo("Lobby armado")
                .payloadResumen("Tu scrim #" + ev.scrimId() + " completó el cupo.")
                .build();
        notifier.notifyAllChannels(n);
    }
}
