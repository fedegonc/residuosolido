package com.residuosolido.app.model;

import java.util.Locale;

/**
 * Normalización del código de seguimiento de solicitudes de invitado.
 * Los códigos se generan en mayúsculas (alfabeto sin caracteres ambiguos);
 * un invitado que lo copia a mano puede escribirlo en minúsculas, y la
 * búsqueda en Mongo es case-sensitive — sin canonicalización el tracking
 * devolvía lista vacía en silencio. Mismo patrón que {@link Username}.
 */
public final class TrackingCode {

    private TrackingCode() {}

    /** Forma canónica: sin espacios de borde, en mayúsculas. */
    public static String canonical(String raw) {
        if (raw == null) return null;
        return raw.trim().toUpperCase(Locale.ROOT);
    }
}
