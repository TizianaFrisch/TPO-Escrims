package com.uade...notifications.adapters;

import com.uade...notifications.Notificacion;
import com.uade...notifications.Notifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class PushNotifier implements Notifier {
    private final RestTemplate rest = new RestTemplate();

    @Value("${fcm.serverKey:}")
    private String serverKey;

    @Override
    public void send(Notificacion n) {
        if (n.getDestinoPushToken() == null || n.getDestinoPushToken().isBlank()) return;
        if (serverKey == null || serverKey.isBlank()) return;

        var url = "https://fcm.googleapis.com/fcm/send"; // Legacy (fácil y suficiente para el TPO)
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key=" + serverKey);

        var body = Map.of(
                "to", n.getDestinoPushToken(),
                "notification", Map.of(
                        "title", "eScrims: " + n.getTipo(),
                        "body", n.getPayloadResumen()
                )
        );

        try {
            rest.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception ignored) { /* log + retry si querés */ }
    }
}
