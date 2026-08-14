package com.residuosolido.app.model;

public record Name(String value) {

    private static final int MAX_LENGTH = 100;

    public Name {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("error.name.required");
        }
        String normalized = value.trim();
        if (normalized.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("error.name.too_long");
        }
        value = normalized;
    }

    public static Name of(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("error.name.required");
        }
        return new Name(raw);
    }

    public static boolean isValid(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        return raw.trim().length() <= MAX_LENGTH;
    }

    @Override
    public String toString() {
        return value;
    }
}
