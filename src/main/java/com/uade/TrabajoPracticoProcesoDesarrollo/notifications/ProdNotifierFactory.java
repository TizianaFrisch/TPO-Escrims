package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters.DiscordNotifier;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters.EmailNotifier;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters.PushNotifier;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Fábrica de notifiers para ambientes productivos y consola.
 * Implementaciones son stubs: dejan trazas diferenciadas por canal.
 * Se activa con profile "prod" o "console"; en otros perfiles seguirá DevNotifierFactory.
 */
@Component
@Profile({"prod", "console"})
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
            return discord;
    }

    @Override
    public Notifier createEmail() {
            return email;
    }

    @Override
    public Notifier createPush() {
            return push;
    }
}
