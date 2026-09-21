package com.residuosolido.app.model;

import com.residuosolido.app.enums.ServerMessage;
import com.residuosolido.app.exception.ValidationException;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilidad de validación y normalización de teléfonos a formato E.164.
 * Antes era un record; ahora es una clase de métodos estáticos porque nadie
 * usaba el tipo PhoneNumber — todos llamaban .value() inmediatamente.
 */
public final class PhoneNumber {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+][1-9][0-9]{6,14}$");

    /** Separadores visuales que la gente escribe a mano (espacios, guiones, paréntesis) — nunca dígitos válidos. */
    private static final Pattern DECORATIVE_CHARS = Pattern.compile("[\\s\\-()]");

    /** Reglas por país: dialCode → fullNationalLength. */
    private static final Map<String, Integer> COUNTRY_RULES = Map.of(
            "+598", 8,   // Uruguay: 8 dígitos nacionales
            "+55", 11    // Brasil: DDD (2) + 9 nacionales
    );

    private PhoneNumber() {}

    /**
     * Valida y normaliza un número E.164 (ej: "+598 99 123 456" → "+59899123456").
     * Lanza IllegalArgumentException si el formato es inválido.
     */
    public static String normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
        String normalized = DECORATIVE_CHARS.matcher(raw).replaceAll("");
        if (raw.length() > 32 || !PHONE_PATTERN.matcher(normalized).matches()) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID);
        }
        return normalized;
    }

    /**
     * Factory country-aware: valida el número nacional según las reglas del país.
     * - Uruguay (+598): quita el 0 inicial doméstico, exige 8 dígitos con primer dígito 9.
     * - Brasil (+55): antepone el DDD (2 dígitos), exige 9 dígitos nacionales con primer dígito 9.
     * Devuelve un String en formato E.164 completo.
     */
    public static String normalize(String dialCode, String national, String ddd) {
        if (!COUNTRY_RULES.containsKey(dialCode)) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_UNSUPPORTED_COUNTRY);
        }
        if (national == null || national.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
        String cleaned = DECORATIVE_CHARS.matcher(national.trim()).replaceAll("");

        // Uruguay: quitar 0 inicial doméstico (ej: "092224955" → "92224955")
        if ("+598".equals(dialCode) && cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1);
        }

        // Quitar prefijo internacional si el usuario lo incluyó
        boolean hadDialCode = false;
        if (cleaned.startsWith(dialCode)) {
            cleaned = cleaned.substring(dialCode.length());
            hadDialCode = true;
        }

        if (!cleaned.matches("[0-9]+")) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID);
        }

        String fullNational;
        if ("+55".equals(dialCode)) {
            if (hadDialCode) {
                fullNational = cleaned;
            } else {
                String dddCleaned = (ddd == null || ddd.trim().isEmpty()) ? "55" : DECORATIVE_CHARS.matcher(ddd.trim()).replaceAll("");
                if (!dddCleaned.matches("[0-9]{2}")) {
                    throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID_DDD);
                }
                fullNational = dddCleaned + cleaned;
            }
        } else {
            fullNational = cleaned;
        }

        int expectedLength = COUNTRY_RULES.get(dialCode);
        if (fullNational.length() != expectedLength) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID_LENGTH);
        }

        // Validar primer dígito del número nacional (9 para celular)
        String nationalPart = "+55".equals(dialCode) ? fullNational.substring(2) : fullNational;
        if (!nationalPart.startsWith("9")) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID_FIRST_DIGIT);
        }

        return dialCode + fullNational;
    }

    public static boolean isValid(String raw) {
        try {
            normalize(raw);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
