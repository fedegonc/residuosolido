package com.residuosolido.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
class RateLimiterTest {

    private HttpServletRequest requestFrom(String ip) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(ip);
        return request;
    }

    // ========== Por IP (antes GuestRateLimiterTest) ==========

    @Test
    void isAllowed_underLimit_returnsTrue() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("1.2.3.4");
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.isAllowed(request));
        }
    }

    @Test
    void isAllowed_overLimit_returnsFalse() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("1.2.3.5");
        for (int i = 0; i < 5; i++) {
            limiter.isAllowed(request);
        }
        assertFalse(limiter.isAllowed(request));
    }

    @Test
    void isAllowed_differentIps_trackedIndependently() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest requestA = requestFrom("1.2.3.6");
        HttpServletRequest requestB = requestFrom("1.2.3.7");
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.isAllowed(requestA));
        }
        assertFalse(limiter.isAllowed(requestA));
        assertTrue(limiter.isAllowed(requestB));
    }

    @Test
    void isAllowed_usesXForwardedForHeader_whenPresent() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("9.9.9.9, 10.0.0.1");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.isAllowed(request));
        }
        assertFalse(limiter.isAllowed(request));
    }

    @Test
    void ipLimiter_shutdown_clearsState() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("1.2.3.8");
        for (int i = 0; i < 5; i++) {
            limiter.isAllowed(request);
        }
        assertFalse(limiter.isAllowed(request));

        limiter.shutdown();
        assertTrue(limiter.isAllowed(request));
    }

    // ========== Por IP (login) ==========

    @Test
    void isBlocked_belowMaxAttempts_returnsFalse() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("2.3.4.5");
        limiter.loginFailed(request);
        limiter.loginFailed(request);
        assertFalse(limiter.isBlocked(request));
    }

    @Test
    void isBlocked_atMaxAttempts_returnsTrue() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("2.3.4.6");
        limiter.loginFailed(request);
        limiter.loginFailed(request);
        limiter.loginFailed(request);
        assertTrue(limiter.isBlocked(request));
    }

    @Test
    @SuppressWarnings("unchecked")
    void isBlocked_afterLockExpires_returnsFalseAndClearsState() throws Exception {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("2.3.4.7");
        limiter.loginFailed(request);
        limiter.loginFailed(request);
        limiter.loginFailed(request);
        assertTrue(limiter.isBlocked(request), "Debe estar bloqueado justo después del 3er intento");

        Field lockedUntilField = RateLimiter.class.getDeclaredField("loginLockedUntil");
        lockedUntilField.setAccessible(true);
        Map<String, AtomicLong> lockedUntil = (Map<String, AtomicLong>) lockedUntilField.get(limiter);
        lockedUntil.get("2.3.4.7").set(System.currentTimeMillis() - 1);

        assertFalse(limiter.isBlocked(request), "El lock debe expirar una vez pasado su timestamp");

        limiter.loginFailed(request);
        assertFalse(limiter.isBlocked(request), "Tras expirar, el contador de intentos debe haber vuelto a cero");
    }

    @Test
    void loginSucceeded_resetsAttempts() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("2.3.4.8");
        limiter.loginFailed(request);
        limiter.loginFailed(request);
        limiter.loginFailed(request);
        assertTrue(limiter.isBlocked(request));

        limiter.loginSucceeded(request);
        assertFalse(limiter.isBlocked(request));
    }

    @Test
    void isBlocked_unknownIp_returnsFalse() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("9.9.9.9");
        assertFalse(limiter.isBlocked(request));
    }

    @Test
    void isBlocked_differentIps_trackedIndependently() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest requestA = requestFrom("2.3.4.9");
        HttpServletRequest requestB = requestFrom("2.3.4.10");
        limiter.loginFailed(requestA);
        limiter.loginFailed(requestA);
        limiter.loginFailed(requestA);
        assertTrue(limiter.isBlocked(requestA));
        assertFalse(limiter.isBlocked(requestB));
    }

    @Test
    void loginFailed_nullIp_doesNotThrow() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("2.3.4.12");
        assertDoesNotThrow(() -> limiter.loginFailed(request));
        assertDoesNotThrow(() -> limiter.isBlocked(request));
    }

    @Test
    void loginLimiter_shutdown_clearsAllState() {
        RateLimiter limiter = new RateLimiter();
        HttpServletRequest request = requestFrom("2.3.4.11");
        limiter.loginFailed(request);
        limiter.loginFailed(request);
        limiter.loginFailed(request);
        assertTrue(limiter.isBlocked(request));

        limiter.shutdown();
        assertFalse(limiter.isBlocked(request));
    }
}
