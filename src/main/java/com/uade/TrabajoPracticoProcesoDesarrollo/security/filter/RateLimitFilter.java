package com.uade.security.filter;

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

    // Por defecto: 100 requests por minuto por “clave”
    @Value("${app.ratelimit.capacity:100}")
    private int capacity;

    @Value("${app.ratelimit.periodSeconds:60}")
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
