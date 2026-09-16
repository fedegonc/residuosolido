package com.residuosolido.app.model;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilidad de validación y normalización de teléfonos a formato E.164.
 * Antes era un record; ahora es una clase de métodos estáticos porque nadie
 * usaba el tipo PhoneNumber — todos llamaban .value() inmediatamente.
 */
public final class PhoneNumber {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+][1-9][0-9]{6,14}$");

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
            throw new IllegalArgumentException("error.phone.required");
        }
        String normalized = raw.replaceAll("\\s+", "");
        if (raw.length() > 32 || !PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("error.phone.invalid");
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
            throw new IllegalArgumentException("error.phone.unsupported_country");
        }
        if (national == null || national.trim().isEmpty()) {
            throw new IllegalArgumentException("error.phone.required");
        }
        String cleaned = national.trim().replaceAll("[\\s-]", "");

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
            throw new IllegalArgumentException("error.phone.invalid");
        }

        String fullNational;
        if ("+55".equals(dialCode)) {
            if (hadDialCode) {
                fullNational = cleaned;
            } else {
                String dddCleaned = (ddd == null || ddd.trim().isEmpty()) ? "55" : ddd.trim().replaceAll("[\\s-]", "");
                if (!dddCleaned.matches("[0-9]{2}")) {
                    throw new IllegalArgumentException("error.phone.invalid_ddd");
                }
                fullNational = dddCleaned + cleaned;
            }
        } else {
            fullNational = cleaned;
        }

        int expectedLength = COUNTRY_RULES.get(dialCode);
        if (fullNational.length() != expectedLength) {
            throw new IllegalArgumentException("error.phone.invalid_length");
        }

        // Validar primer dígito del número nacional (9 para celular)
        String nationalPart = "+55".equals(dialCode) ? fullNational.substring(2) : fullNational;
        if (!nationalPart.startsWith("9")) {
            throw new IllegalArgumentException("error.phone.invalid_first_digit");
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
