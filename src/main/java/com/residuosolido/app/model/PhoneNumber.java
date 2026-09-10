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

    /**
     * Factory country-aware: valida el número nacional según las reglas del país.
     * - Uruguay: quita el 0 inicial doméstico, exige 8 dígitos con primer dígito 9.
     * - Brasil: antepone el DDD (2 dígitos), exige 9 dígitos nacionales con primer dígito 9.
     * Devuelve un PhoneNumber en formato E.164 completo.
     */
    public static PhoneNumber of(CountryCode country, String national, String ddd) {
        if (national == null || national.trim().isEmpty()) {
            throw new IllegalArgumentException("error.phone.required");
        }
        String cleaned = national.trim().replaceAll("[\\s-]", "");

        // Uruguay: quitar 0 inicial doméstico (ej: "092224955" → "92224955")
        if (country == CountryCode.URUGUAY && cleaned.startsWith("0")) {
            cleaned = cleaned.substring(1);
        }

        // Quitar prefijo internacional si el usuario lo incluyó
        boolean hadDialCode = false;
        if (cleaned.startsWith(country.getDialCode())) {
            cleaned = cleaned.substring(country.getDialCode().length());
            hadDialCode = true;
        }

        if (!cleaned.matches("[0-9]+")) {
            throw new IllegalArgumentException("error.phone.invalid");
        }

        String fullNational;
        if (country == CountryCode.BRAZIL) {
            if (hadDialCode) {
                // Si el usuario incluyó el dial code, el DDD ya está en el número
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

        if (fullNational.length() != country.getFullNationalLength()) {
            throw new IllegalArgumentException("error.phone.invalid_length");
        }

        // Validar primer dígito del número nacional (9 para celular)
        // Para Brasil, el primer dígito nacional es el tercer dígito de fullNational (después del DDD)
        String nationalPart = country == CountryCode.BRAZIL
                ? fullNational.substring(2)
                : fullNational;
        if (!nationalPart.startsWith("9")) {
            throw new IllegalArgumentException("error.phone.invalid_first_digit");
        }

        return new PhoneNumber(country.getDialCode() + fullNational);
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
