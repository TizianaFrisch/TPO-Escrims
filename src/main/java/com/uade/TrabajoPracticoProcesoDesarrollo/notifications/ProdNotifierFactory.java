package com.uade.TrabajoPracticoProcesoDesarrollo.notifications;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters.DiscordNotifier;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters.EmailNotifier;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters.PushNotifier;
import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters.SendGridEmailNotifier;

import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
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
        private final SendGridEmailNotifier sendGrid;
        private final org.slf4j.Logger log = LoggerFactory.getLogger(ProdNotifierFactory.class);
        @org.springframework.beans.factory.annotation.Value("${sendgrid.apiKey:}")
        private String sendgridApiKey;
   private final PushNotifier push;

   public ProdNotifierFactory(DiscordNotifier discord, EmailNotifier email, PushNotifier push, SendGridEmailNotifier sendGrid) {
           this.discord = discord;
           this.email = email;
           this.push = push;
           this.sendGrid = sendGrid;
   }

   @PostConstruct
   private void init() {
           // Log which channels appear available (adapters soft-fail if not configured)
           log.info("ProdNotifierFactory initialized. SendGrid configured={}", sendgridApiKey != null && !sendgridApiKey.isBlank());
   }

    @Override
    public Notifier createChat() {
            return discord;
    }

    @Override
    public Notifier createEmail() {
            if (sendgridApiKey != null && !sendgridApiKey.isBlank() && sendGrid != null) {
                return sendGrid;
            }
            return email;
    }

    @Override
    public Notifier createPush() {
            return push;
    }
}
