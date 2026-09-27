package com.residuosolido.app.model;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Entidad de autenticación. Modela tanto ciudadanos (USER) como organizaciones
 * (ORGANIZATION) en una misma tabla/colección, diferenciados por el campo
 * {@link #role}. El perfil de negocio de las organizaciones vive en la
 * colección separada {@code organizations} (ver {@link Organization}).
 *
 * Los campos de contacto (email, teléfono, nombre, ciudad) se validan y
 * canonicalizan en sus setters, garantizando que el modelo nunca contenga
 * valores inválidos. Para organizaciones, estos campos son opcionales desde el
 * punto de vista de User; los datos operativos se mantienen en Organization.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "password")
@Document(collection = "users")
public class User {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    /* sparse=true: el registro ya no pide email (pide teléfono en su lugar, ver
       RegistrationForm/UserRegistrationService) — sin sparse, el índice único
       rechazaría el 2do usuario con email=null. */
    @Indexed(unique = true, sparse = true, collation = "{'locale':'en','strength':2}")
    private String email;
    private String password;

    private Role role;

    private String firstName;
    private String phone;

    private City city;

    private LocalDateTime createdAt;
    private boolean active = true;

    /**
     * Setea el email validándolo y normalizándolo a minúsculas.
     * Lanza IllegalArgumentException si el formato es inválido.
     */
    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            this.email = null;
            return;
        }
        String normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_EMAIL_INVALID);
        }
        this.email = normalized;
    }

    /**
     * Setea el nombre validándolo.
     * Lanza IllegalArgumentException si está vacío o excede 100 caracteres.
     */
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            this.firstName = null;
            return;
        }
        String trimmed = firstName.trim();
        if (trimmed.length() > 100) {
            throw new ValidationException(ServerMessage.ERROR_NAME_TOO_LONG);
        }
        this.firstName = trimmed;
    }

    /**
     * Setea el teléfono validándolo y canonicalizándolo a E.164 via {@link PhoneNumber}.
     * Acepta formatos con espacios (ej: "+598 99 123 456") y los normaliza.
     * Lanza IllegalArgumentException si el formato es inválido.
     */
    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            this.phone = null;
            return;
        }
        this.phone = PhoneNumber.normalize(phone);
    }

    public String getDisplayName() {
        return firstName != null && !firstName.isBlank() ? firstName : username;
    }

    public boolean isOrganization() {
        return role == Role.ORGANIZATION;
    }

    public boolean hasPhone() {
        return phone != null && !phone.isBlank();
    }

    public boolean hasCity() {
        return city != null;
    }

}
