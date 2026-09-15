package com.residuosolido.app.model;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de usuario. Modela tanto ciudadanos (USER) como organizaciones (ORGANIZATION)
 * en una misma tabla/colección, diferenciados por el campo {@link #role}.
 * Usuarios y organizaciones comparten atributos básicos; acceptedMaterials y city
 * son relevantes principalmente para organizaciones.
 *
 * Los campos de contacto (email, teléfono, nombre) se validan y canonicalizan
 * en sus setters, delegando a Value Objects ({@link Email}, {@link PhoneNumber},
 * {@link Name}). Esto garantiza que el modelo nunca contenga valores inválidos.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = "password")
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true, collation = "{'locale':'en','strength':2}")
    private String email;
    private String password;

    private Role role;

    private String firstName;
    private String phone;

    private City city;

    private LocalDateTime createdAt;
    private boolean active = true;

    private Boolean profileCompleted = false;

    private List<MaterialCategory> acceptedMaterials = new ArrayList<>();

    /**
     * Setea el email validándolo y normalizándolo via {@link Email}.
     * Lanza IllegalArgumentException si el formato es inválido.
     */
    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            this.email = null;
            return;
        }
        this.email = Email.of(email).value();
    }

    /**
     * Devuelve el email como Value Object tipado {@link Email}.
     * @return Email o null si no tiene email
     */
    public Email getEmailAddress() {
        return this.email == null ? null : Email.of(this.email);
    }

    /**
     * Setea el nombre validándolo via {@link Name}.
     * Lanza IllegalArgumentException si está vacío o excede 100 caracteres.
     */
    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            this.firstName = null;
            return;
        }
        this.firstName = Name.of(firstName).value();
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
        this.phone = PhoneNumber.of(phone).value();
    }

    /**
     * Devuelve el teléfono como Value Object tipado {@link PhoneNumber}.
     * @return PhoneNumber o null si no tiene teléfono
     */
    public PhoneNumber getPhoneNumber() {
        return hasPhone() ? PhoneNumber.of(phone) : null;
    }

    public String getDisplayName() {
        return firstName != null && !firstName.isBlank() ? firstName : username;
    }

    public boolean isOrganization() {
        return role == Role.ORGANIZATION;
    }

    public boolean isProfileComplete() {
        return role != null && role.isProfileComplete(this);
    }

    public boolean hasPhone() {
        return phone != null && !phone.isBlank();
    }

    public boolean hasCity() {
        return city != null;
    }

    public void completeProfile() {
        if (!hasPhone()) {
            throw new IllegalStateException("error.profile.phone_required");
        }
        if (!hasCity()) {
            throw new IllegalStateException("error.profile.city_required");
        }
        this.profileCompleted = true;
    }

    public boolean needsProfileCompletion() {
        return !isProfileComplete();
    }

    public String getAcceptedMaterialsCsv() {
        if (acceptedMaterials == null || acceptedMaterials.isEmpty()) return "";
        return acceptedMaterials.stream()
                .map(Enum::name)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

}
