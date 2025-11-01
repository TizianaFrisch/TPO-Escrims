package com.uade.TrabajoPracticoProcesoDesarrollo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RateLimitingInterceptor.class);

    private final RateLimiter limiter;

    public RateLimitingInterceptor(RateLimiter limiter) {
        this.limiter = limiter;
    }

    private String extractIp(HttpServletRequest request) {
        var xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            // may contain comma-separated list; take first
            var parts = xf.split(",");
            if (parts.length > 0) return parts[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserKey(@Nullable Authentication auth) {
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        try {
            if (principal instanceof com.uade.TrabajoPracticoProcesoDesarrollo.domain.entities.Usuario u) {
                return "id:" + (u.getId() == null ? u.getUsername() : u.getId());
            }
        } catch (NoClassDefFoundError | Exception ignored) {
            // ignore - fallback to name
        }
        var name = auth.getName();
        return name == null ? null : "name:" + name;
    }

    private void respondTooMany(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Too Many Requests\"}");
        response.getWriter().flush();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String ip = extractIp(request);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userKey = extractUserKey(auth);

        // Check user limit first (if present)
        if (userKey != null) {
            boolean ok = limiter.allowUser(userKey);
            if (!ok) {
                log.warn("Rate limit exceeded for user {} on {}", userKey, uri);
                respondTooMany(response);
                return false;
            }
        }

        // Always check IP limit as fallback/parallel protection
        boolean okIp = limiter.allowIp(ip);
        if (!okIp) {
            log.warn("Rate limit exceeded for IP {} on {}", ip, uri);
            respondTooMany(response);
            return false;
        }

        return true;
    }
}
