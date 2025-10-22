package com.uade.TrabajoPracticoProcesoDesarrollo.security.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Por defecto: 100 requests por ventana por "clave"
    // Use a raw string here and parse defensively because some environments may supply a comment-like
    // value such as "100#tokensporventana" which would fail direct int conversion.
    @Value("${app.ratelimit.capacity:100}")
    private String capacityRaw;

    // parsed numeric capacity used at runtime
    private int capacity;

    @Value("${app.ratelimit.periodSeconds:60}")
    private String periodSecondsRaw;

    // parsed numeric period in seconds
    private int periodSeconds;

    @Value("${app.ratelimit.keyStrategy:ip}") // ip | user
    private String keyStrategy;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String key = resolveKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, this::newBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"rate_limited\",\"key\":\"" + key + "\"}");
        }
    }

    private Bucket newBucket(String k) {
        Refill refill = Refill.greedy(capacity, Duration.ofSeconds(periodSeconds));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    @jakarta.annotation.PostConstruct
    private void initParsedValues() {
        // parse capacityRaw to integer, be tolerant to values like "100#tokensporventana" or "100 tokens"
        if (capacityRaw == null) {
            this.capacity = 100;
        } else {
            String digits = capacityRaw.strip().replaceAll("[^0-9].*$", ""); // keep leading digits only
            if (digits.isBlank()) {
                try {
                    this.capacity = Integer.parseInt(capacityRaw.trim());
                } catch (NumberFormatException ex) {
                    this.capacity = 100;
                }
            } else {
                try {
                    this.capacity = Integer.parseInt(digits);
                } catch (NumberFormatException ex) {
                    this.capacity = 100;
                }
            }
        }

        // parse periodSecondsRaw similarly
        if (periodSecondsRaw == null) {
            this.periodSeconds = 60;
        } else {
            String digitsP = periodSecondsRaw.strip().replaceAll("[^0-9].*$", "");
            if (digitsP.isBlank()) {
                try {
                    this.periodSeconds = Integer.parseInt(periodSecondsRaw.trim());
                } catch (NumberFormatException ex) {
                    this.periodSeconds = 60;
                }
            } else {
                try {
                    this.periodSeconds = Integer.parseInt(digitsP);
                } catch (NumberFormatException ex) {
                    this.periodSeconds = 60;
                }
            }
        }
    }

    private String resolveKey(HttpServletRequest req) {
        if ("user".equalsIgnoreCase(keyStrategy)) {
            // Si tenés JWT/Principal, tomá el user; si no existe, cae a IP
            String user = req.getUserPrincipal() != null ? req.getUserPrincipal().getName() : null;
            if (user != null && !user.isBlank()) return "user:" + user;
        }
        // IP por defecto (considera X-Forwarded-For si estás detrás de proxy)
        String xff = req.getHeader("X-Forwarded-For");
        String ip = (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
        return "ip:" + ip;
    }
}
