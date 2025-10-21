package com.uade...notifications.adapters;

import com.uade...notifications.Notificacion;
import com.uade...notifications.Notifier;
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
    public void send(Notificacion n) {
        if (webhookUrl == null || webhookUrl.isBlank()) return; // soft-fail
        var payload = Map.of("content", ":video_game: **" + n.getTipo() + "** — " + n.getPayloadResumen());
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
