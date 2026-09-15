package com.residuosolido.app.model;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Value Object para direcciones de correo electrónico.
 *
 * Valida el formato básico, normaliza a minúsculas y canonicaliza.
 * No es una validación SMTP, pero evita que entren strings inválidos.
 */
public record Email(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MAX_LENGTH = 254;

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("error.register.email_invalid");
        }
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        if (trimmed.length() > MAX_LENGTH || !PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("error.register.email_invalid");
        }
        value = trimmed;
    }

    /**
     * Factory method. Lanza IllegalArgumentException si el email es inválido.
     */
    public static Email of(String value) {
        return new Email(value);
    }

    /**
     * Verifica si un string es un email potencialmente válido sin lanzar excepción.
     */
    public static boolean isValid(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        return trimmed.length() <= MAX_LENGTH && PATTERN.matcher(trimmed).matches();
    }

    @Override
    public String toString() {
        return value;
    }
}
