package com.residuosolido.app.model;

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

/**
 * Entidad de autenticación. Modela tanto ciudadanos (USER) como organizaciones
 * (ORGANIZATION) en una misma tabla/colección, diferenciados por el campo
 * {@link #role}. El perfil de negocio de las organizaciones vive en la
 * colección separada {@code organizations} (ver {@link Organization}).
 *
 * Los campos de contacto (email, teléfono, nombre) se canonicalizan y validan
 * en el boundary de escritura via {@code UserValidator} (registro, perfil,
 * seed) — los setters son planos; Mongo hidrata por field-access sin pasar
 * por ellos, así que nunca fueron la barrera real del invariante.
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
