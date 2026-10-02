package com.residuosolido.app.service;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Username;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Patrón Template Method: lógica común de registro para User y Organization.
 * Ambos necesitan: validar username, canonicalizar, hashear PIN, chequear duplicados.
 *
 * Generic T = User | Organization
 * Subclases implementan: validación entity-específica, repository access, inserción.
 */
public abstract class BaseRegistrationService<T> {

    protected final PasswordEncoder passwordEncoder;

    protected BaseRegistrationService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Flujo de registro — igual para ambas entidades.
     * 1. Validar entity específica
     * 2. Canonicalizar username
     * 3. Validar PIN
     * 4. Chequear duplicado username
     * 5. Hashear PIN
     * 6. Insertar en BD
     */
    public final T register(T entity) {
        ServerMessage error = validateEntity(entity);
        if (error != null) throw new ValidationException(error);

        T prepared = prepareEntity(entity);
        return insertEntity(prepared);
    }

    /**
     * Prepara entity: canonicaliza, hashea PIN, chequea duplicados.
     * Llamado DESPUÉS de validación entity-específica.
     */
    protected T prepareEntity(T entity) {
        String canonicalUsername = canonicalUsername(getUsername(entity));

        // Chequear duplicado
        if (findByUsername(canonicalUsername).isPresent()) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_USERNAME_EXISTS);
        }

        // Hashear PIN
        String hashedPin = passwordEncoder.encode(getPassword(entity));

        // Actualizar entity
        setUsername(entity, canonicalUsername);
        setPassword(entity, hashedPin);

        return entity;
    }

    /**
     * Valida username (largo) y PIN (formato 4 dígitos).
     * Subclases pueden agregar validaciones adicionales.
     */
    protected ServerMessage validateUsernameAndPin(String username, String pin) {
        if (username == null || username.trim().isEmpty()) {
            return ServerMessage.ERROR_REGISTER_USERNAME_REQUIRED;
        }
        String canonical = Username.canonical(username);
        if (canonical.length() > 64) {
            return ServerMessage.ERROR_REGISTER_USERNAME_TOO_LONG;
        }
        try {
            validatePin(pin);
        } catch (ValidationException e) {
            return e.key();
        }
        return null;
    }

    protected String canonicalUsername(String raw) {
        return Username.canonical(raw);
    }

    protected void validatePin(String value) {
        if (value == null || !value.matches("\\d{4}")) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_PIN_INVALID);
        }
    }

    // ===== Template method hooks — subclases implementan =====

    /**
     * Validación entity-específica (email para User, tipo para Organization, etc.)
     * @return ServerMessage si hay error, null si es válido
     */
    protected abstract ServerMessage validateEntity(T entity);

    /**
     * Obtener username de la entity
     */
    protected abstract String getUsername(T entity);

    /**
     * Obtener password (PIN sin hashear) de la entity
     */
    protected abstract String getPassword(T entity);

    /**
     * Setear username canonicalizado en la entity
     */
    protected abstract void setUsername(T entity, String username);

    /**
     * Setear password (PIN hasheado) en la entity
     */
    protected abstract void setPassword(T entity, String hashedPin);

    /**
     * Buscar entity por username (chequear duplicado)
     */
    protected abstract Optional<T> findByUsername(String username);

    /**
     * Insertar entity preparada en BD
     */
    protected abstract T insertEntity(T entity);
}
