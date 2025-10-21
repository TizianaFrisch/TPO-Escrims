package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DevNotifierFactory implements NotifierFactory {

    private static final Logger log = LoggerFactory.getLogger(DevNotifierFactory.class);

    private static class ConsoleNotifier implements Notifier {
        private final String channel;
        ConsoleNotifier(String channel){ this.channel = channel; }
        @Override public void send(String to, String message) {
            // In dev, just log to console
            log.info("[{}] -> {} :: {}", channel, to, message);
        }
    }


    private static class LoggingNotifier implements Notifier {
        private final String channel;
        LoggingNotifier(String channel){ this.channel = channel; }
        @Override public void send(Notificacion n) {
            System.out.println("[DEV " + channel + "] " + n.getTipo() + " :: " + n.getPayloadResumen());
        }
    }

    @Override
    public Notifier createPush() { return new ConsoleNotifier("PUSH"); }

    @Override
    public Notifier createEmail() { return new ConsoleNotifier("EMAIL"); }

    @Override
    public Notifier createChat() { return new ConsoleNotifier("DISCORD"); }
}
