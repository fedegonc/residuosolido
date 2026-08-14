package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para la validación server-side (RN-10) en RequestService.
 * No mockea RequestService — instancia el servicio real con dependencias mockeadas
 * para verificar que las validaciones se ejecutan efectivamente.
 */
class RequestServiceValidationTest {

    private RequestRepository requestRepository;
    private CityOrgService cityOrgService;
    private LocalImageService imageService;
    private RequestValidator validator;
    private OrgResolver orgResolver;
    private RequestService requestService;
    private RequestUpdateService requestUpdateService;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        cityOrgService = mock(CityOrgService.class);
        imageService = mock(LocalImageService.class);
        validator = new RequestValidator();
        orgResolver = new OrgResolver(cityOrgService);
        requestService = new RequestService(requestRepository, imageService, validator, orgResolver);
        requestUpdateService = new RequestUpdateService(requestRepository, validator, orgResolver, imageService);
    }

    // ─── materials null ───

    @Test
    void rn10_createRequest_nullMaterials_throwsIllegalArgumentException() {
        User user = new User();
        user.setId("u1");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        user, City.RIVERA, "Calle 123", null,
                        null, null, null, "org1", null, null)
        );
        assertEquals("error.request.materials_required", ex.getMessage());
    }

    // ─── materials lista vacía ───

    @Test
    void rn10_createRequest_emptyMaterials_throwsIllegalArgumentException() {
        User user = new User();
        user.setId("u1");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        user, City.RIVERA, "Calle 123", null,
                        Collections.emptyList(), null, null, "org1", null, null)
        );
        assertEquals("error.request.materials_required", ex.getMessage());
    }

    // ─── materials null en updateRequest ───

    @Test
    void rn10_updateRequest_nullMaterials_throwsIllegalArgumentException() {
        User user = new User();
        user.setId("u1");

        com.residuosolido.app.model.Request existing = new com.residuosolido.app.model.Request();
        existing.setId("req1");
        existing.setUser(user);
        existing.setStatus(com.residuosolido.app.enums.RequestStatus.PENDING);

        when(requestRepository.findById("req1"))
                .thenReturn(java.util.Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestUpdateService.updateRequest(
                        "req1", user, City.RIVERA, "Calle 123", null,
                        null, null)
        );
        assertEquals("error.request.materials_required", ex.getMessage());
    }

    // ─── materials lista vacía en updateRequest ───

    @Test
    void rn10_updateRequest_emptyMaterials_throwsIllegalArgumentException() {
        User user = new User();
        user.setId("u1");

        com.residuosolido.app.model.Request existing = new com.residuosolido.app.model.Request();
        existing.setId("req1");
        existing.setUser(user);
        existing.setStatus(com.residuosolido.app.enums.RequestStatus.PENDING);

        when(requestRepository.findById("req1"))
                .thenReturn(java.util.Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestUpdateService.updateRequest(
                        "req1", user, City.RIVERA, "Calle 123", null,
                        Collections.emptyList(), null)
        );
        assertEquals("error.request.materials_required", ex.getMessage());
    }

    // ─── address vacío también (cobertura adicional) ───

    @Test
    void rn10_createRequest_emptyAddress_throwsIllegalArgumentException() {
        User user = new User();
        user.setId("u1");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        user, City.RIVERA, "", null,
                        List.of(MaterialCategory.PLASTICO), null, null, "org1", null, null)
        );
        assertEquals("error.request.address_required", ex.getMessage());
    }

    // ─── city null ───

    @Test
    void rn10_createRequest_nullCity_throwsIllegalArgumentException() {
        User user = new User();
        user.setId("u1");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        user, null, "Calle 123", null,
                        List.of(MaterialCategory.PLASTICO), null, null, "org1", null, null)
        );
        assertEquals("error.request.city_required", ex.getMessage());
    }

    // ─── guest sin nombre ───

    @Test
    void rn10_createRequest_guestWithoutName_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        null, City.RIVERA, "Calle 123", null,
                        List.of(MaterialCategory.PLASTICO), "", "+59899123456", "org1", null, null)
        );
        assertEquals("error.name.required", ex.getMessage());
    }

    // ─── guest sin teléfono ───

    @Test
    void rn10_createRequest_guestWithoutPhone_throwsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        null, City.RIVERA, "Calle 123", null,
                        List.of(MaterialCategory.PLASTICO), "Juan", "", "org1", null, null)
        );
        assertEquals("error.phone.required", ex.getMessage());
    }

    // ─── materials válidos NO lanzan excepción ───

    @Test
    void rn10_createRequest_validMaterials_doesNotThrowOnValidation() {
        User user = new User();
        user.setId("u1");

        User org = new User();
        org.setId("org1");
        org.setRole(com.residuosolido.app.enums.Role.ORGANIZATION);
        org.setCity(City.RIVERA);

        when(cityOrgService.findOrganizationByIdAndCity("org1", City.RIVERA))
                .thenReturn(org);
        when(requestRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        com.residuosolido.app.model.Request result = requestService.createRequest(
                user, City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL),
                null, null, "org1", null, null);

        assertNotNull(result);
        assertEquals(2, result.getMaterials().size());
    }
}
