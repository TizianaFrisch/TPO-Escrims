package com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Notifier;

@Component
public class PushNotifier implements Notifier {
    private final RestTemplate rest = new RestTemplate();

    @Value("${fcm.serverKey:}")
    private String serverKey;

    @Override
    public boolean send(String to, String message) {
        if (to == null || to.isBlank()) return false;
        if (serverKey == null || serverKey.isBlank()) return false;

        var url = "https://fcm.googleapis.com/fcm/send"; // Legacy (fácil y suficiente para el TPO)
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key=" + serverKey);

    var body = Map.of(
        "to", to,
        "notification", Map.of(
            "title", "eScrims: " + to,
            "body", message
        )
    );

        try {
            var resp = rest.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            return resp != null && resp.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            // log optionally
            return false;
        }
    }
}
