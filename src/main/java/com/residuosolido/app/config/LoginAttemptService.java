package com.residuosolido.app.config;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;
    private static final long CLEANUP_THRESHOLD_MS = 300_000L; // 5 min
    private static final long STALE_ATTEMPT_MS = LOCK_DURATION_MS;

    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> lockedUntil = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastAttemptAt = new ConcurrentHashMap<>();
    private volatile long lastCleanup = System.currentTimeMillis();

    public void loginFailed(String username) {
        cleanupStaleEntries();
        String key = key(username);
        int count = attempts.merge(key, 1, Integer::sum);
        lastAttemptAt.put(key, System.currentTimeMillis());
        if (count >= MAX_ATTEMPTS) {
            lockedUntil.put(key, new AtomicLong(System.currentTimeMillis() + LOCK_DURATION_MS));
        }
    }

    public void loginSucceeded(String username) {
        String key = key(username);
        attempts.remove(key);
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
            attempts.remove(key);
            lockedUntil.remove(key);
            lastAttemptAt.remove(key);
            return false;
        }
        return true;
    }

    /**
     * Elimina entradas de intentos fallidos que nunca escalaron a bloqueo y
     * quedaron huérfanas (el usuario no volvió a intentar loguearse), evitando
     * un crecimiento sin límite de los mapas en instancias de larga duración.
     */
    private void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < CLEANUP_THRESHOLD_MS) {
            return;
        }
        lastCleanup = now;
        Iterator<Map.Entry<String, Long>> it = lastAttemptAt.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            AtomicLong until = lockedUntil.get(entry.getKey());
            boolean lockExpired = until != null && now > until.get();
            boolean staleWithoutLock = until == null && now - entry.getValue() > STALE_ATTEMPT_MS;
            if (lockExpired || staleWithoutLock) {
                attempts.remove(entry.getKey());
                lockedUntil.remove(entry.getKey());
                it.remove();
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        attempts.clear();
        lockedUntil.clear();
        lastAttemptAt.clear();
    }

    private String key(String username) {
        return username == null ? "" : username.toLowerCase();
    }
}
