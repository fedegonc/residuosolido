package com.residuosolido.app.model;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+][1-9][0-9]{6,14}$");

    public PhoneNumber {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("error.phone.required");
        }
        String normalized = value.replaceAll("\\s+", "");
        if (value.length() > 32 || !PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("error.phone.invalid");
        }
        value = normalized;
    }

    public static PhoneNumber of(String raw) {
        return new PhoneNumber(raw);
    }

    public static boolean isValid(String raw) {
        try {
            of(raw);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
