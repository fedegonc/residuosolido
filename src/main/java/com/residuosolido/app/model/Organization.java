package com.residuosolido.app.model;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.OrgType;
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
import java.io.Serializable;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Entidad de negocio autónoma: Organización de acopio/reciclaje.
 * COMPLETAMENTE SEPARADA de User (ciudadano).
 * Tiene su propio login: username + PIN (4 dígitos).
 *
 * Separación limpia:
 * - Organizations tabla: empresas, municipios, acopios
 * - Users tabla: ciudadanos (crean solicitudes)
 * - Se comunican por ID en Request (userId + organizationId)
 *
 * SIN acoplamiento por ID, SIN role derivado.
 */
@Document(collection = "organizations")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"acceptedMaterials", "password"})
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    // ===== AUTENTICACIÓN (propio login) =====
    @Indexed(unique = true)
    private String username;
    private String password;  // PIN hasheado (4 dígitos)

    // ===== DATOS DE NEGOCIO =====
    private String name;
    private OrgType tipo;
    private String phone;
    private City city;

    private List<MaterialCategory> acceptedMaterials = new ArrayList<>();
    private Boolean profileCompleted = false;

    private LocalDateTime createdAt;
    private boolean active = true;

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
