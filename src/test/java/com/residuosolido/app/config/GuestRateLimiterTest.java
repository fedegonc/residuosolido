package com.residuosolido.app.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GuestRateLimiterTest {

    private HttpServletRequest requestFrom(String ip) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(ip);
        return request;
    }

    @Test
    void isAllowed_underLimit_returnsTrue() {
        GuestRateLimiter limiter = new GuestRateLimiter();
        HttpServletRequest request = requestFrom("1.2.3.4");
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.isAllowed(request));
        }
    }

    @Test
    void isAllowed_overLimit_returnsFalse() {
        GuestRateLimiter limiter = new GuestRateLimiter();
        HttpServletRequest request = requestFrom("1.2.3.5");
        for (int i = 0; i < 5; i++) {
            limiter.isAllowed(request);
        }
        assertFalse(limiter.isAllowed(request));
    }

    @Test
    void isAllowed_differentIps_trackedIndependently() {
        GuestRateLimiter limiter = new GuestRateLimiter();
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
        GuestRateLimiter limiter = new GuestRateLimiter();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("9.9.9.9, 10.0.0.1");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        for (int i = 0; i < 5; i++) {
            assertTrue(limiter.isAllowed(request));
        }
        assertFalse(limiter.isAllowed(request));
    }

    @Test
    void shutdown_clearsState() {
        GuestRateLimiter limiter = new GuestRateLimiter();
        HttpServletRequest request = requestFrom("1.2.3.8");
        for (int i = 0; i < 5; i++) {
            limiter.isAllowed(request);
        }
        assertFalse(limiter.isAllowed(request));

        limiter.shutdown();
        assertTrue(limiter.isAllowed(request));
    }
}
