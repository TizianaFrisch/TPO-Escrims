package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Fábrica de notifiers para ambientes productivos.
 * Implementaciones son stubs: dejan trazas diferenciadas por canal.
 * Se activa con profile "prod"; en otros perfiles seguirá DevNotifierFactory.
 */
@Component
@Profile("prod")
public class ProdNotifierFactory implements NotifierFactory {
   private final DiscordNotifier discord;
   private final EmailNotifier email;
   private final PushNotifier push;

   public ProdNotifierFactory(DiscordNotifier discord, EmailNotifier email, PushNotifier push) {
       this.discord = discord;
       this.email = email;
       this.push = push;
           }

    @Override
    public Notifier createChat() {
            return /* stub */;
            return discord;
    }

    @Override
    public Notifier createEmail() {
             return /* stub */;
            return email;
    }

    @Override
    public Notifier createPush() {
            return /* stub */;
            return push;
    }
}
