package com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Adapters;

import com.uade.TrabajoPracticoProcesoDesarrollo.notifications.Notifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
public class DiscordNotifier implements Notifier {
    private final WebClient client;
    @Value("${integrations.discord.webhookUrl:}")
    private String webhookUrl;

    public DiscordNotifier() {
        this.client = WebClient.builder().build();
    }

    @Override
    public void send(String to, String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) return; // soft-fail
        // Si to se usa como tipo, lo incluimos en el mensaje
        var content = ":video_game: **" + to + "** — " + message;
        var payload = Map.of("content", content);
        client.post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(ex -> Mono.empty())
                .block(); // si preferís 100% async, retorná el Mono y manejalo fuera
    }
}
