package com.residuosolido.app.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

final class AccountInput {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private AccountInput() {
    }

    static String email(String value) {
        if (value == null || value.trim().length() > 254 || !EMAIL.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("error.register.email_invalid");
        }
        return value.trim().toLowerCase(Locale.ROOT);
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
