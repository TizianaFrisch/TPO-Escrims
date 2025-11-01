package com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters;

import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Simple SendGrid API adapter (v3) for sending mail via SendGrid HTTP API.
 * If `sendgrid.apiKey` is not configured, this notifier will return false.
 */
@Component
public class SendGridEmailNotifier implements Notifier {
    private static final Logger log = LoggerFactory.getLogger(SendGridEmailNotifier.class);
    private final RestTemplate rest = new RestTemplate();

    @Value("${sendgrid.apiKey:}")
    private String apiKey;

    @Value("${spring.mail.from:eScrims@uade.com}")
    private String from;

    @Override
    public boolean send(String to, String message) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("SendGrid API key not configured, skipping");
            return false;
        }
        if (to == null || to.isBlank()) return false;

        String subject = "[eScrims] Notificación";
        String body = message;
        int sep = message != null ? message.indexOf(":") : -1;
        if (message != null && sep > 0) {
            String maybeTitle = message.substring(0, sep).trim();
            String maybeBody = message.substring(sep + 1).trim();
            if (!maybeTitle.isEmpty()) {
                subject = "[eScrims] " + maybeTitle;
                body = maybeBody.isEmpty() ? message : maybeBody;
            }
        }

        var payload = Map.of(
                "personalizations", List.of(Map.of("to", List.of(Map.of("email", to)))),
                "from", Map.of("email", from),
                "subject", subject,
                "content", List.of(Map.of("type", "text/plain", "value", body))
        );

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        try {
            var resp = rest.postForEntity("https://api.sendgrid.com/v3/mail/send", new HttpEntity<>(payload, headers), String.class);
            boolean ok = resp != null && (resp.getStatusCode().is2xxSuccessful() || resp.getStatusCode() == org.springframework.http.HttpStatus.ACCEPTED);
            if (ok) log.info("SendGrid: email sent to {} (subject={})", to, subject);
            else log.warn("SendGrid: unexpected response {} sending to {}", resp != null ? resp.getStatusCode() : "null", to);
            return ok;
        } catch (Exception ex) {
            log.error("SendGrid send failed to {}: {}", to, ex.getMessage());
            return false;
        }
    }
}
