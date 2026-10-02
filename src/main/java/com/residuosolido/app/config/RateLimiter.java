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
 * Limita abuso por IP (previene DoS):
 * - Registro: N requests por minuto por IP (ventana deslizante)
 * - Login: N intentos fallidos por IP en 15 min
 *
 * Cambio de versión anterior: bloqueo por IP, no por username.
 * Razón: bloqueo por username permite DoS (atacante bloquea "admin" para todos).
 * Bloqueo por IP es local y defensivo: protege contra ataques de esa IP.
 */
@Component
public class RateLimiter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000L;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;
    private static final long CLEANUP_THRESHOLD_MS = 300_000L; // 5 min

    private final ConcurrentHashMap<String, Deque<Long>> ipTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> loginFailures = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> loginLockedUntil = new ConcurrentHashMap<>();
    private volatile long lastCleanup = System.currentTimeMillis();

    // ========== Por IP (registro) ==========

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

    // ========== Por IP (login) ==========

    public void loginFailed(HttpServletRequest request) {
        cleanupStaleEntries();
        String ip = request.getRemoteAddr();
        int count = loginFailures.merge(ip, 1, Integer::sum);
        if (count >= MAX_LOGIN_ATTEMPTS) {
            loginLockedUntil.put(ip, new AtomicLong(System.currentTimeMillis() + LOCK_DURATION_MS));
        }
    }

    public void loginSucceeded(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        loginFailures.remove(ip);
        loginLockedUntil.remove(ip);
    }

    public boolean isBlocked(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        AtomicLong until = loginLockedUntil.get(ip);
        if (until == null) {
            return false;
        }
        if (System.currentTimeMillis() > until.get()) {
            loginFailures.remove(ip);
            loginLockedUntil.remove(ip);
            return false;
        }
        return true;
    }

    // ========== Barrido compartido ==========

    /**
     * Barre ambos mecanismos: entradas de IP vencidas y bloqueos de login expirados.
     */
    private void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < CLEANUP_THRESHOLD_MS) {
            return;
        }
        lastCleanup = now;

        // Limpiar timestamps de requests por IP
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

        // Limpiar bloqueos de login expirados
        Iterator<Map.Entry<String, AtomicLong>> lockIt = loginLockedUntil.entrySet().iterator();
        while (lockIt.hasNext()) {
            AtomicLong until = lockIt.next().getValue();
            if (now > until.get()) {
                String ip = lockIt.next().getKey();
                loginFailures.remove(ip);
                lockIt.remove();
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        ipTimestamps.clear();
        loginFailures.clear();
        loginLockedUntil.clear();
    }
}
