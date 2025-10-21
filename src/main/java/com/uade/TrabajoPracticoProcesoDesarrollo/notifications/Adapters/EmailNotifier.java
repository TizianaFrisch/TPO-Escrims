package com.uade...notifications.adapters;

import com.uade...notifications.Notificacion;
import com.uade...notifications.Notifier;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailNotifier implements Notifier {
    private final JavaMailSender mailSender;
    @Value("${spring.mail.from:eScrims@uade.com}")
    private String from;

    public EmailNotifier(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(Notificacion n) {
        if (n.getDestinoEmail() == null || n.getDestinoEmail().isBlank()) return;
        var msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(n.getDestinoEmail());
        msg.setSubject("[eScrims] " + n.getTipo());
        msg.setText(n.getPayloadTextoPlano());
        try {
            mailSender.send(msg);
        } catch (Exception ignored) { /* log + retry si querés */ }
    }
}
