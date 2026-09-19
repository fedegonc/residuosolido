package com.residuosolido.app.config;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Limita abuso por dos vías independientes que comparten el mismo esqueleto
 * (mapa concurrente + ventana temporal + barrido periódico de entradas viejas):
 * - Por IP: solicitudes de invitados y registro (ventana deslizante, N por minuto).
 * - Por usuario: intentos de login fallidos (bloqueo temporal tras N intentos).
 * Antes eran GuestRateLimiter + LoginAttemptService por separado.
 */
@Component
public class RateLimiter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000L;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;
    private static final long CLEANUP_THRESHOLD_MS = 300_000L; // 5 min

    private final ConcurrentHashMap<String, Deque<Long>> ipTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> lockedUntil = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastAttemptAt = new ConcurrentHashMap<>();
    private volatile long lastCleanup = System.currentTimeMillis();

    // ========== Por IP (invitados, registro) ==========

    public boolean isAllowed(HttpServletRequest request) {
        return isAllowed(request, "requests");
    }

    public boolean isAllowed(HttpServletRequest request, String scope) {
        cleanupStaleEntries();
        String ip = scope + ":" + request.getRemoteAddr();
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

    // ========== Por usuario (login) ==========

    public void loginFailed(String username) {
        cleanupStaleEntries();
        String key = key(username);
        int count = loginAttempts.merge(key, 1, Integer::sum);
        lastAttemptAt.put(key, System.currentTimeMillis());
        if (count >= MAX_LOGIN_ATTEMPTS) {
            lockedUntil.put(key, new AtomicLong(System.currentTimeMillis() + LOCK_DURATION_MS));
        }
    }

    public void loginSucceeded(String username) {
        String key = key(username);
        loginAttempts.remove(key);
        lockedUntil.remove(key);
        lastAttemptAt.remove(key);
    }

    public boolean isBlocked(String username) {
        String key = key(username);
        AtomicLong until = lockedUntil.get(key);
        if (until == null) {
            return false;
        }
        if (System.currentTimeMillis() > until.get()) {
            loginAttempts.remove(key);
            lockedUntil.remove(key);
            lastAttemptAt.remove(key);
            return false;
        }
        return true;
    }

    private String key(String username) {
        return username == null ? "" : username.toLowerCase();
    }

    // ========== Barrido compartido ==========

    /**
     * Barre ambos mecanismos en el mismo ciclo: entradas de IP vencidas y
     * entradas de login huérfanas que nunca escalaron a bloqueo (el usuario
     * no volvió a intentar), evitando crecimiento sin límite de los mapas.
     */
    private void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < CLEANUP_THRESHOLD_MS) {
            return;
        }
        lastCleanup = now;

        Iterator<Map.Entry<String, Deque<Long>>> ipIt = ipTimestamps.entrySet().iterator();
        while (ipIt.hasNext()) {
            Deque<Long> deque = ipIt.next().getValue();
            synchronized (deque) {
                deque.removeIf(ts -> now - ts > WINDOW_MS);
                if (deque.isEmpty()) {
                    ipIt.remove();
                }
            }
        }

        Iterator<Map.Entry<String, Long>> loginIt = lastAttemptAt.entrySet().iterator();
        while (loginIt.hasNext()) {
            Map.Entry<String, Long> entry = loginIt.next();
            AtomicLong until = lockedUntil.get(entry.getKey());
            boolean lockExpired = until != null && now > until.get();
            boolean staleWithoutLock = until == null && now - entry.getValue() > LOCK_DURATION_MS;
            if (lockExpired || staleWithoutLock) {
                loginAttempts.remove(entry.getKey());
                lockedUntil.remove(entry.getKey());
                loginIt.remove();
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        ipTimestamps.clear();
        loginAttempts.clear();
        lockedUntil.clear();
        lastAttemptAt.clear();
    }
}
