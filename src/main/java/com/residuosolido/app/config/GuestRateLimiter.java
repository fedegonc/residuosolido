package com.residuosolido.app.config;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class GuestRateLimiter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000L;
    private static final long CLEANUP_THRESHOLD_MS = 300_000L; // 5 min
    private long lastCleanup = System.currentTimeMillis();

    private final ConcurrentHashMap<String, Deque<Long>> ipTimestamps = new ConcurrentHashMap<>();

    public boolean isAllowed(HttpServletRequest request) {
        cleanupStaleEntries();
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

    private void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < CLEANUP_THRESHOLD_MS) {
            return;
        }
        lastCleanup = now;
        Iterator<Map.Entry<String, Deque<Long>>> it = ipTimestamps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Deque<Long>> entry = it.next();
            Deque<Long> deque = entry.getValue();
            synchronized (deque) {
                deque.removeIf(ts -> now - ts > WINDOW_MS);
                if (deque.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        ipTimestamps.clear();
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
