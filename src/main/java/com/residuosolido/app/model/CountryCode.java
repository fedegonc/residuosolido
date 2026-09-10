package com.residuosolido.app.model;

/**
 * Códigos de país soportados para validación de teléfonos.
 * Uruguay (+598): 8 dígitos nacionales, primer dígito 9 (celular).
 * Brasil (+55): DDD de 2 dígitos + 9 dígitos nacionales, primer dígito 9.
 */
public enum CountryCode {
    URUGUAY("+598", 8),
    BRAZIL("+55", 11);

    private final String dialCode;
    private final int fullNationalLength;

    CountryCode(String dialCode, int fullNationalLength) {
        this.dialCode = dialCode;
        this.fullNationalLength = fullNationalLength;
    }

    public String getDialCode() {
        return dialCode;
    }

    public int getFullNationalLength() {
        return fullNationalLength;
    }

    public static CountryCode fromDialCode(String dialCode) {
        for (CountryCode cc : values()) {
            if (cc.dialCode.equals(dialCode)) {
                return cc;
            }
        }
        throw new IllegalArgumentException("error.phone.unsupported_country");
    }
}
