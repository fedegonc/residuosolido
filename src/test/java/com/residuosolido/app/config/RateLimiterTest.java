package com.residuosolido.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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

    // ========== Por usuario (antes LoginAttemptServiceTest) ==========

    @Test
    void isBlocked_belowMaxAttempts_returnsFalse() {
        RateLimiter limiter = new RateLimiter();
        limiter.loginFailed("user1");
        limiter.loginFailed("user1");
        assertFalse(limiter.isBlocked("user1"));
    }

    @Test
    void isBlocked_atMaxAttempts_returnsTrue() {
        RateLimiter limiter = new RateLimiter();
        limiter.loginFailed("user1");
        limiter.loginFailed("user1");
        limiter.loginFailed("user1");
        assertTrue(limiter.isBlocked("user1"));
    }

    @Test
    void loginSucceeded_resetsAttempts() {
        RateLimiter limiter = new RateLimiter();
        limiter.loginFailed("user1");
        limiter.loginFailed("user1");
        limiter.loginFailed("user1");
        assertTrue(limiter.isBlocked("user1"));

        limiter.loginSucceeded("user1");
        assertFalse(limiter.isBlocked("user1"));
    }

    @Test
    void isBlocked_unknownUser_returnsFalse() {
        RateLimiter limiter = new RateLimiter();
        assertFalse(limiter.isBlocked("nobody"));
    }

    @Test
    void key_isCaseInsensitive() {
        RateLimiter limiter = new RateLimiter();
        limiter.loginFailed("User1");
        limiter.loginFailed("user1");
        limiter.loginFailed("USER1");
        assertTrue(limiter.isBlocked("uSeR1"));
    }

    @Test
    void loginFailed_nullUsername_doesNotThrow() {
        RateLimiter limiter = new RateLimiter();
        assertDoesNotThrow(() -> limiter.loginFailed(null));
        assertDoesNotThrow(() -> limiter.isBlocked(null));
    }

    @Test
    void loginLimiter_shutdown_clearsAllState() {
        RateLimiter limiter = new RateLimiter();
        limiter.loginFailed("user1");
        limiter.loginFailed("user1");
        limiter.loginFailed("user1");
        assertTrue(limiter.isBlocked("user1"));

        limiter.shutdown();
        assertFalse(limiter.isBlocked("user1"));
    }
}
