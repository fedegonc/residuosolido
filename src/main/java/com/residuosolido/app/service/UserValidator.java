package com.residuosolido.app.service;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.PhoneNumber;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Canonicalización y validación de campos de contacto de {@code User}.
 * Funciones puras (sin estado): devuelven el valor canonical o lanzan
 * {@link ValidationException} con la clave i18n correspondiente.
 *
 * Los setters de User son planos (Lombok) — la regla vive acá y la aplican
 * los servicios en el boundary de escritura (registro, edición de perfil,
 * seed). El modelo en sí es un contenedor de datos; las colecciones Mongo
 * se hidratan por field-access sin pasar por setters, así que la validación
 * en setter nunca fue la barrera real.
 */
public final class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private UserValidator() {
    }

    /** Email canonical (trim + lowercase) o null si viene vacío. */
    public static String canonicalEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_EMAIL_INVALID);
        }
        return normalized;
    }

    /** Nombre canonical (trim) o null si viene vacío. */
    public static String canonicalName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            return null;
        }
        String trimmed = firstName.trim();
        if (trimmed.length() > 100) {
            throw new ValidationException(ServerMessage.ERROR_NAME_TOO_LONG);
        }
        return trimmed;
    }

    /** Teléfono canonical (E.164 via PhoneNumber) o null si viene vacío. */
    public static String canonicalPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return PhoneNumber.normalize(phone);
    }
}
