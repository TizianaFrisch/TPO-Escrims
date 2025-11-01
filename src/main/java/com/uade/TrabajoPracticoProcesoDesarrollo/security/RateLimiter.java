package com.uade.TrabajoPracticoProcesoDesarrollo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory fixed-window rate limiter.
 * Keyed by arbitrary string (IP or user). Not distributed. Good for low-priority protections.
 */
@Component
public class RateLimiter {
    private static class Counter {
        volatile long windowStartSec;
        final AtomicInteger count = new AtomicInteger(0);
    }

    private final ConcurrentHashMap<String, Counter> map = new ConcurrentHashMap<>();

    @Value("${app.ratelimit.windowSeconds:60}")
    private int windowSeconds;

    @Value("${app.ratelimit.maxPerIp:30}")
    private int maxPerIp;

    @Value("${app.ratelimit.maxPerUser:10}")
    private int maxPerUser;

    @Value("${app.ratelimit.enabled:true}")
    private boolean enabled;

    /**
     * Check per-IP allowance.
     */
    public boolean allowIp(String ip) {
        if (!enabled) return true;
        if (ip == null) return true; // allow if unknown
        return allowForKey("IP:" + ip, maxPerIp);
    }

    /**
     * Check per-user allowance. userKey can be numeric id or username.
     */
    public boolean allowUser(String userKey) {
        if (!enabled) return true;
        if (userKey == null) return true;
        return allowForKey("USER:" + userKey, maxPerUser);
    }

    private boolean allowForKey(String key, int max) {
        long now = System.currentTimeMillis() / 1000L;
        var c = map.computeIfAbsent(key, k -> {
            Counter cc = new Counter();
            cc.windowStartSec = now;
            return cc;
        });

        synchronized (c) {
            if (now - c.windowStartSec >= windowSeconds) {
                c.windowStartSec = now;
                c.count.set(0);
            }
            int val = c.count.incrementAndGet();
            return val <= max;
        }
    }
}
