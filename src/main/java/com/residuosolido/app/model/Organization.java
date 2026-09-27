package com.residuosolido.app.model;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.StateException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Perfil de negocio de una organización de acopio, separado del usuario de
 * autenticación {@link User}. El {@link User} con role=ORGANIZATION es la
 * identidad de login; esta entidad contiene los datos operativos (ciudad,
 * teléfono, materiales aceptados, onboarding completado).
 *
 * El id coincide con el id del User dueño para simplificar referencias.
 * Email y username se leen del User asociado, evitando denormalización
 * y riesgos de desincronización.
 */
@Document(collection = "organizations")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"acceptedMaterials"})
public class Organization {

    @Id
    private String id;

    private String name;

    private String phone;

    private City city;

    private List<MaterialCategory> acceptedMaterials = new ArrayList<>();

    private Boolean profileCompleted = false;

    public boolean hasPhone() {
        return phone != null && !phone.isBlank();
    }

    public boolean hasCity() {
        return city != null;
    }

    public boolean isProfileComplete() {
        return hasPhone() && hasCity() && Boolean.TRUE.equals(profileCompleted);
    }

    public boolean needsProfileCompletion() {
        return !isProfileComplete();
    }

    public void completeProfile() {
        if (!hasPhone()) {
            throw new StateException(ServerMessage.ERROR_PROFILE_PHONE_REQUIRED);
        }
        if (!hasCity()) {
            throw new StateException(ServerMessage.ERROR_PROFILE_CITY_REQUIRED);
        }
        this.profileCompleted = true;
    }

    /** Fallback de displayName: nombre o id. */
    public String getDisplayName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return id;
    }

    /** Formato CSV para el frontend (filterMaterialsByOrg). */
    public String getAcceptedMaterialsCsv() {
        return acceptedMaterials.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }
}
