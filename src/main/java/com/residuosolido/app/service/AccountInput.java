package com.residuosolido.app.service;

import java.nio.charset.StandardCharsets;

final class AccountInput {
    private AccountInput() {
    }

    static void password(String value) {
        if (value == null || value.isBlank() || value.length() < 8) {
            throw new IllegalArgumentException("error.register.password_min_length");
        }
        if (value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("error.register.password_too_long");
        }
    }
}
