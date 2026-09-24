package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestValidator: validaciones de Request")
class RequestValidatorTest {

    private RequestValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RequestValidator();
    }

    @Test
    @DisplayName("validateCreate: ciudadano válido + campos obligatorios")
    void validateCreateCitizenValid() {
        User user = new User();
        user.setRole(Role.USER);
        user.setActive(true);
        user.setPhone("+59899123456");

        assertDoesNotThrow(() -> validator.validateCreate(
            user, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), null, null, "org123"
        ));
    }

    @Test
    @DisplayName("validateCreate: ciudadano sin teléfono válido → error")
    void validateCreateCitizenNoPhone() {
        User user = new User();
        user.setRole(Role.USER);
        user.setActive(true);
        user.setPhone(null);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            user, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), null, null, "org123"
        ));
        assertEquals(ServerMessage.ERROR_PROFILE_PHONE_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: ciudadano con rol ORGANIZATION → error")
    void validateCreateNotCitizen() {
        User user = new User();
        user.setRole(Role.ORGANIZATION);
        user.setActive(true);
        user.setPhone("+59899123456");

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            user, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), null, null, "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_CITIZEN_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: invitado válido")
    void validateCreateGuestValid() {
        assertDoesNotThrow(() -> validator.validateCreate(
            null, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), "Juan", "+59899123456", "org123"
        ));
    }

    @Test
    @DisplayName("validateCreate: invitado sin nombre → error")
    void validateCreateGuestNoName() {
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            null, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), null, "+59899123456", "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_GUEST_NAME_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: invitado sin teléfono → error")
    void validateCreateGuestNoPhone() {
        assertThrows(ValidationException.class, () -> validator.validateCreate(
            null, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), "Juan", null, "org123"
        ));
    }

    @Test
    @DisplayName("validateCreate: sin ciudad → error")
    void validateCreateNoCity() {
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            null, null, "Calle 123", List.of(MaterialCategory.PLASTICO), "Juan", "+59899123456", "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_CITY_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: sin dirección → error")
    void validateCreateNoAddress() {
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            null, City.RIVERA, null, List.of(MaterialCategory.PLASTICO), "Juan", "+59899123456", "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_ADDRESS_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: sin materiales → error")
    void validateCreateNoMaterials() {
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            null, City.RIVERA, "Calle 123", List.of(), "Juan", "+59899123456", "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_MATERIALS_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: sin organización → error")
    void validateCreateNoOrganization() {
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            null, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), "Juan", "+59899123456", null
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateMaterials: organización acepta todos")
    void validateMaterialsValid() {
        User org = new User();
        org.setAcceptedMaterials(List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.VIDRIO));

        assertDoesNotThrow(() -> validator.validateMaterials(
            org, List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL)
        ));
    }

    @Test
    @DisplayName("validateMaterials: organización no acepta alguno → error")
    void validateMaterialsNotAccepted() {
        User org = new User();
        org.setAcceptedMaterials(List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL));

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateMaterials(
            org, List.of(MaterialCategory.PLASTICO, MaterialCategory.VIDRIO)
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_MATERIALS_NOT_ACCEPTED, ex.key());
    }

    @Test
    @DisplayName("validateUpdate: campos válidos")
    void validateUpdateValid() {
        assertDoesNotThrow(() -> validator.validateUpdate(
            City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), "org123"
        ));
    }
}
