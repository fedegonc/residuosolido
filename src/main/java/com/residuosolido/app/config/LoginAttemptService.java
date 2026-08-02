package com.residuosolido.app.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;

    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> lockedUntil = new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        String key = key(username);
        int count = attempts.merge(key, 1, Integer::sum);
        if (count >= MAX_ATTEMPTS) {
            lockedUntil.put(key, new AtomicLong(System.currentTimeMillis() + LOCK_DURATION_MS));
        }
    }

    public void loginSucceeded(String username) {
        String key = key(username);
        attempts.remove(key);
        lockedUntil.remove(key);
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
            return false;
        }
        return true;
    }

    private String key(String username) {
        return username == null ? "" : username.toLowerCase();
    }
}
