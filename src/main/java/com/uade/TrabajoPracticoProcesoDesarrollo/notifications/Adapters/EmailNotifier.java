package com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters;

import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailNotifier implements Notifier {
    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    @Value("${spring.mail.from:eScrims@uade.com}")
    private String from;
    @Value("${app.notifications.email.enabled:true}")
    private boolean enabled;

    public EmailNotifier(ObjectProvider<JavaMailSender> mailSenderProvider) { this.mailSenderProvider = mailSenderProvider; }

    @Override
    public void send(String to, String message) {
        if (!enabled) {
            log.debug("Email notifications disabled by config (app.notifications.email.enabled=false). Skipping send to {}", to);
            return;
        }
        log.info("EmailNotifier.send() called - to: {}, message: {}", to, message);
        if (to == null || to.isBlank()) {
            log.warn("Email recipient is null or blank, skipping");
            return;
        }
        String subject = "[eScrims] Notificación";
        String body = message;
        // If message is in format "Titulo: contenido", use Titulo as subject
        int sep = message != null ? message.indexOf(":") : -1;
        if (message != null && sep > 0) {
            String maybeTitle = message.substring(0, sep).trim();
            String maybeBody = message.substring(sep + 1).trim();
            if (!maybeTitle.isEmpty()) {
                subject = "[eScrims] " + maybeTitle;
                body = maybeBody.isEmpty() ? message : maybeBody;
            }
        }

        var msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("No JavaMailSender bean available in context. Skipping email send to {}.", to);
            return;
        }
        try {
            log.info("Attempting to send email from {} to {} via {}", from, to, sender.getClass().getSimpleName());
            sender.send(msg);
            log.info("✅ Email sent successfully to {} (subject={})", to, msg.getSubject());
        } catch (Exception ex) {
            log.error("❌ Failed to send email to {}: {}", to, ex.getMessage(), ex);
        }
    }
}
