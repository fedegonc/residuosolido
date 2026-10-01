package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("RequestValidator: validaciones de Request")
class RequestValidatorTest {

    private RequestValidator validator;
    private OrganizationRepository organizationRepository;

    @BeforeEach
    void setUp() {
        // existsById=false por defecto → el user de prueba es ciudadano
        organizationRepository = mock(OrganizationRepository.class);
        validator = new RequestValidator(organizationRepository);
    }

    @Test
    @DisplayName("validateCreate: ciudadano válido + campos obligatorios")
    void validateCreateCitizenValid() {
        User user = new User();
        user.setActive(true);
        user.setPhone("+59899123456");

        assertDoesNotThrow(() -> validator.validateCreate(
            user, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), "org123"
        ));
    }

    @Test
    @DisplayName("validateCreate: ciudadano sin teléfono válido → error")
    void validateCreateCitizenNoPhone() {
        User user = new User();
        user.setActive(true);
        user.setPhone(null);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            user, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), "org123"
        ));
        assertEquals(ServerMessage.ERROR_PROFILE_PHONE_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: usuario con doc Organization → error")
    void validateCreateNotCitizen() {
        User user = new User();
        user.setId("org-user");
        user.setActive(true);
        user.setPhone("+59899123456");
        when(organizationRepository.existsById("org-user")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            user, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_CITIZEN_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: sin usuario (null) → error")
    void validateCreateNoUser() {
        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            null, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_CITIZEN_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: sin ciudad → error")
    void validateCreateNoCity() {
        User user = new User();
        user.setActive(true);
        user.setPhone("+59899123456");

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            user, null, "Calle 123", List.of(MaterialCategory.PLASTICO), "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_CITY_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: sin dirección → error")
    void validateCreateNoAddress() {
        User user = new User();
        user.setActive(true);
        user.setPhone("+59899123456");

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            user, City.RIVERA, null, List.of(MaterialCategory.PLASTICO), "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_ADDRESS_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: sin materiales → error")
    void validateCreateNoMaterials() {
        User user = new User();
        user.setActive(true);
        user.setPhone("+59899123456");

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            user, City.RIVERA, "Calle 123", List.of(), "org123"
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_MATERIALS_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateCreate: sin organización → error")
    void validateCreateNoOrganization() {
        User user = new User();
        user.setActive(true);
        user.setPhone("+59899123456");

        ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateCreate(
            user, City.RIVERA, "Calle 123", List.of(MaterialCategory.PLASTICO), null
        ));
        assertEquals(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("validateMaterials: organización acepta todos")
    void validateMaterialsValid() {
        Organization org = new Organization();
        org.setAcceptedMaterials(List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL, MaterialCategory.VIDRIO));

        assertDoesNotThrow(() -> validator.validateMaterials(
            org, List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL)
        ));
    }

    @Test
    @DisplayName("validateMaterials: organización no acepta alguno → error")
    void validateMaterialsNotAccepted() {
        Organization org = new Organization();
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
