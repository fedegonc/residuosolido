package com.residuosolido.app.model;

import java.util.Locale;

/**
 * Normalización de nombres de usuario para usos que deben ser
 * case-insensitive (unicidad en registro, clave de lockout, lookup de login).
 *
 * No valida formato ni longitud: eso sigue en {@link UserRegistrationService}.
 */
public final class Username {

    private Username() {}

    /**
     * Forma canónica: sin espacios de borde, en minúsculas, sin colapsar
     * espacios internos (los nombres compuestos se conservan).
     */
    public static String canonical(String raw) {
        if (raw == null) return null;
        return raw.trim().toLowerCase(Locale.ROOT);
    }
}
