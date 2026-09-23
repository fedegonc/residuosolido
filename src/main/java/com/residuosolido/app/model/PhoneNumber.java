package com.residuosolido.app.model;

import com.residuosolido.app.exception.ServerMessage;
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
     * Selector binacional del formulario: si vienen código de país y número
     * nacional, normaliza con las reglas del país; si no, devuelve el valor
     * crudo tal cual (null-safe). Única fuente para la resolución de teléfono
     * en formularios (registro, perfil de org, solicitud).
     */
    public static String resolve(String dialCode, String national, String ddd, String raw) {
        if (national != null && !national.isBlank() && dialCode != null && !dialCode.isBlank()) {
            return normalize(dialCode, national, ddd);
        }
        return raw;
    }

    /**
     * Factory country-aware: valida el número nacional según las reglas del país.
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
        String fullNational = "+598".equals(dialCode)
                ? normalizeUruguay(cleaned)
                : normalizeBrazil(cleaned, ddd);
        return dialCode + fullNational;
    }

    /** Uruguay (+598): quita el 0 inicial doméstico y un prefijo +598 repetido; exige 8 dígitos. */
    private static String normalizeUruguay(String cleaned) {
        if (cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.startsWith("+598")) {
            cleaned = cleaned.substring(4);
        }
        return validateNational(cleaned, 8, 0);
    }

    /** Brasil (+55): si no vino el código de país, antepone el DDD (default "55"); exige 11 dígitos. */
    private static String normalizeBrazil(String cleaned, String ddd) {
        boolean hadDialCode = cleaned.startsWith("+55");
        if (hadDialCode) {
            cleaned = cleaned.substring(3);
        }
        if (!cleaned.matches("[0-9]+")) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID);
        }
        if (hadDialCode) {
            return validateNational(cleaned, 11, 2);
        }
        String dddCleaned = (ddd == null || ddd.trim().isEmpty())
                ? "55"
                : DECORATIVE_CHARS.matcher(ddd.trim()).replaceAll("");
        if (!dddCleaned.matches("[0-9]{2}")) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID_DDD);
        }
        return validateNational(dddCleaned + cleaned, 11, 2);
    }

    /** Largo exacto, solo dígitos y primer dígito nacional 9 (celular). {@code skip} saltea el DDD. */
    private static String validateNational(String digits, int expectedLength, int skip) {
        if (!digits.matches("[0-9]+")) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID);
        }
        if (digits.length() != expectedLength) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID_LENGTH);
        }
        if (!digits.substring(skip).startsWith("9")) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_INVALID_FIRST_DIGIT);
        }
        return digits;
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
