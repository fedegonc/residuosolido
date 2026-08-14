package com.residuosolido.app.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    @Test
    void isBlocked_belowMaxAttempts_returnsFalse() {
        LoginAttemptService service = new LoginAttemptService();
        service.loginFailed("user1");
        service.loginFailed("user1");
        assertFalse(service.isBlocked("user1"));
    }

    @Test
    void isBlocked_atMaxAttempts_returnsTrue() {
        LoginAttemptService service = new LoginAttemptService();
        service.loginFailed("user1");
        service.loginFailed("user1");
        service.loginFailed("user1");
        assertTrue(service.isBlocked("user1"));
    }

    @Test
    void loginSucceeded_resetsAttempts() {
        LoginAttemptService service = new LoginAttemptService();
        service.loginFailed("user1");
        service.loginFailed("user1");
        service.loginFailed("user1");
        assertTrue(service.isBlocked("user1"));

        service.loginSucceeded("user1");
        assertFalse(service.isBlocked("user1"));
    }

    @Test
    void isBlocked_unknownUser_returnsFalse() {
        LoginAttemptService service = new LoginAttemptService();
        assertFalse(service.isBlocked("nobody"));
    }

    @Test
    void key_isCaseInsensitive() {
        LoginAttemptService service = new LoginAttemptService();
        service.loginFailed("User1");
        service.loginFailed("user1");
        service.loginFailed("USER1");
        assertTrue(service.isBlocked("uSeR1"));
    }

    @Test
    void loginFailed_nullUsername_doesNotThrow() {
        LoginAttemptService service = new LoginAttemptService();
        assertDoesNotThrow(() -> service.loginFailed(null));
        assertDoesNotThrow(() -> service.isBlocked(null));
    }

    @Test
    void shutdown_clearsAllState() {
        LoginAttemptService service = new LoginAttemptService();
        service.loginFailed("user1");
        service.loginFailed("user1");
        service.loginFailed("user1");
        assertTrue(service.isBlocked("user1"));

        service.shutdown();
        assertFalse(service.isBlocked("user1"));
    }
}
