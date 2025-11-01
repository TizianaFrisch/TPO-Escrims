package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "local", "test"})
public class DevNotifierFactory implements NotifierFactory {

    private static final Logger log = LoggerFactory.getLogger(DevNotifierFactory.class);

    private static class ConsoleNotifier implements Notifier {
        private final String channel;
        ConsoleNotifier(String channel){ this.channel = channel; }
        @Override public boolean send(String destinoEmail, String payloadTextoPlano) {
            // In dev, just log to console
            log.info("[{}] -> {} :: {}", channel, destinoEmail, payloadTextoPlano);
            return true;
        }
    }


    private static class LoggingNotifier implements Notifier {
        private final String channel;
        LoggingNotifier(String channel){ this.channel = channel; }
        @Override public boolean send(String destinoEmail, String payloadTextoPlano) {
            System.out.println("[DEV " + channel + "] -> " + destinoEmail + " :: " + payloadTextoPlano);
            return true;
        }
    }

    @Override
    public Notifier createPush() { return new ConsoleNotifier("PUSH"); }

    @Override
    public Notifier createEmail() { return new ConsoleNotifier("EMAIL"); }

    @Override
    public Notifier createChat() { return new ConsoleNotifier("DISCORD"); }
}
