package com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters;

import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Notifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(prefix = "app.notifications.email", name = "enabled", havingValue = "true")
public class EmailNotifier implements Notifier {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.from:eScrims@uade.com}")
    private String from;

    public EmailNotifier(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(String to, String message) {
        if (to == null || to.isBlank()) return;
        var msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("[eScrims] " + to);
        msg.setText(message);
        try {
            mailSender.send(msg);
        } catch (Exception ignored) { /* log + retry si querés */ }
    }
}
