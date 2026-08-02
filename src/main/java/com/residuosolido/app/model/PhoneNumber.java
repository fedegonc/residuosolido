package com.residuosolido.app.model;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+][0-9]{1,3}[\\s0-9]{6,15}$");

    public PhoneNumber {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("error.phone.required");
        }
        String normalized = value.trim();
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("error.phone.invalid");
        }
        value = normalized;
    }

    public static PhoneNumber of(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("error.phone.required");
        }
        return new PhoneNumber(raw);
    }

    public static boolean isValid(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(raw.trim()).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}
