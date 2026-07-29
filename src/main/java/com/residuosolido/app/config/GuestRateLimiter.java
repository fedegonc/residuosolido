package com.residuosolido.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class GuestRateLimiter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, Deque<Long>> ipTimestamps = new ConcurrentHashMap<>();

    public boolean isAllowed(HttpServletRequest request) {
        String ip = extractIp(request);
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = ipTimestamps.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());

        synchronized (timestamps) {
            timestamps.removeIf(ts -> now - ts > WINDOW_MS);
            if (timestamps.size() >= MAX_REQUESTS) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
