package com.residuosolido.app.service;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

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
@Tag("unit")
class RequestServiceValidationTest {

    private RequestRepository requestRepository;
    private CityOrgService cityOrgService;
    private LocalImageService imageService;
    private RequestService requestService;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        cityOrgService = mock(CityOrgService.class);
        imageService = mock(LocalImageService.class);
        requestService = new RequestService(requestRepository, imageService, cityOrgService,
                mock(ApplicationEventPublisher.class), new RequestValidator());
    }

    private User citizen() {
        return TestFixtures.citizen("u1", "+59899123456");
    }

    private Organization org() {
        return TestFixtures.organization("org1", City.RIVERA, MaterialCategory.PLASTICO, MaterialCategory.PAPEL);
    }

    // ─── materials null ───

    @Test
    void rn10_createRequest_nullMaterials_throwsIllegalArgumentException() {
        User user = citizen();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        user, City.RIVERA, "Calle 123", null,
                        null, "org1")
        );
        assertEquals("error.request.materials_required", ex.getMessage());
    }

    // ─── materials lista vacía ───

    @Test
    void rn10_createRequest_emptyMaterials_throwsIllegalArgumentException() {
        User user = citizen();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        user, City.RIVERA, "Calle 123", null,
                        Collections.emptyList(), "org1")
        );
        assertEquals("error.request.materials_required", ex.getMessage());
    }

    // ─── materials null en updateRequest ───

    @Test
    void rn10_updateRequest_nullMaterials_throwsIllegalArgumentException() {
        User user = citizen();

        com.residuosolido.app.model.Request existing = new com.residuosolido.app.model.Request();
        existing.setId("req1");
        existing.setContactUser(user);
        existing.restoreStatus(com.residuosolido.app.enums.RequestStatus.PENDING);

        when(requestRepository.findById("req1"))
                .thenReturn(java.util.Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.updateRequest(
                        "req1", user, City.RIVERA, "Calle 123", null,
                        null, "org1", null)
        );
        assertEquals("error.request.materials_required", ex.getMessage());
    }

    // ─── materials lista vacía en updateRequest ───

    @Test
    void rn10_updateRequest_emptyMaterials_throwsIllegalArgumentException() {
        User user = citizen();

        com.residuosolido.app.model.Request existing = new com.residuosolido.app.model.Request();
        existing.setId("req1");
        existing.setContactUser(user);
        existing.restoreStatus(com.residuosolido.app.enums.RequestStatus.PENDING);

        when(requestRepository.findById("req1"))
                .thenReturn(java.util.Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.updateRequest(
                        "req1", user, City.RIVERA, "Calle 123", null,
                        Collections.emptyList(), "org1", null)
        );
        assertEquals("error.request.materials_required", ex.getMessage());
    }

    // ─── organización faltante (asignación no es automática) ───

    @Test
    void rn_createRequest_missingOrganization_throwsIllegalArgumentException() {
        User user = citizen();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        user, City.RIVERA, "Calle 123", null,
                        List.of(MaterialCategory.PLASTICO), null)
        );
        assertEquals("error.request.organization_required", ex.getMessage());
    }

    @Test
    void rn_updateRequest_missingOrganization_throwsIllegalArgumentException() {
        User user = citizen();

        com.residuosolido.app.model.Request existing = new com.residuosolido.app.model.Request();
        existing.setId("req1");
        existing.setContactUser(user);
        existing.restoreStatus(com.residuosolido.app.enums.RequestStatus.PENDING);

        when(requestRepository.findById("req1"))
                .thenReturn(java.util.Optional.of(existing));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.updateRequest(
                        "req1", user, City.RIVERA, "Calle 123", null,
                        List.of(MaterialCategory.PLASTICO), null, null)
        );
        assertEquals("error.request.organization_required", ex.getMessage());
    }

    // ─── address vacío también (cobertura adicional) ───

    @Test
    void rn10_createRequest_emptyAddress_throwsIllegalArgumentException() {
        User user = citizen();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        user, City.RIVERA, "", null,
                        List.of(MaterialCategory.PLASTICO), "org1")
        );
        assertEquals("error.request.address_required", ex.getMessage());
    }

    // ─── city null ───

    @Test
    void rn10_createRequest_nullCity_throwsIllegalArgumentException() {
        User user = citizen();
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        user, null, "Calle 123", null,
                        List.of(MaterialCategory.PLASTICO), "org1")
        );
        assertEquals("error.request.city_required", ex.getMessage());
    }

    // ─── user nulo lanza error (no guests) ───

    @Test
    void rn10_createRequest_nullUser_requiresCitizen() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> requestService.createRequest(
                        null, City.RIVERA, "Calle 123", null,
                        List.of(MaterialCategory.PLASTICO), "org1")
        );
        assertEquals("error.request.citizen_required", ex.getMessage());
    }

    // ─── materials válidos NO lanzan excepción ───

    @Test
    void rn10_createRequest_validMaterials_doesNotThrowOnValidation() {
        User user = citizen();
        Organization org = org();

        when(cityOrgService.findOrganizationByIdAndCity("org1", City.RIVERA))
                .thenReturn(org);
        when(requestRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        com.residuosolido.app.model.Request result = requestService.createRequest(
                user, City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO, MaterialCategory.PAPEL),
                "org1");

        assertNotNull(result);
        assertEquals(2, result.getMaterials().size());
    }

    // ─── Reasignación de organización al editar (CU-U6) ───
    // Regresión crítica para el refactor User/Organization: updateRequest no solo
    // edita campos, sino que puede cambiar la org asignada. Si Request.organization
    // pasa a apuntar a otra colección, este flujo es el primero en romperse.

    @Test
    void rn_updateRequest_reassignsOrganization_whenNewOrgAcceptsMaterials() {
        User user = citizen();
        Organization org1 = TestFixtures.organization("org1", City.RIVERA, MaterialCategory.PLASTICO, MaterialCategory.PAPEL);
        Organization org2 = TestFixtures.organization("org2", City.RIVERA, MaterialCategory.PLASTICO, MaterialCategory.PAPEL);

        com.residuosolido.app.model.Request existing = com.residuosolido.app.model.Request.forCitizen(user);
        existing.setId("req1");
        existing.updateDraft(City.RIVERA, "Calle vieja", null, List.of(MaterialCategory.PLASTICO));
        existing.assignOrganization(org1);

        when(requestRepository.findById("req1"))
                .thenReturn(java.util.Optional.of(existing));
        when(cityOrgService.findOrganizationByIdAndCity("org2", City.RIVERA))
                .thenReturn(org2);
        when(requestRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(imageService.attachImageToRequest(any(), any()))
                .thenAnswer(inv -> inv.getArgument(0));

        com.residuosolido.app.model.Request result = requestService.updateRequest(
                "req1", user, City.RIVERA, "Calle nueva", null,
                List.of(MaterialCategory.PAPEL), "org2", null);

        assertEquals(org2, result.getOrganization(),
                "Al editar, la solicitud debe reasignarse a la nueva organización elegible");
    }

    // ─── RN-11: solo solicitudes PENDING pueden eliminarse ───

    @Test
    void rn11_deleteOwnedRequest_notPending_throwsIllegalStateException() {
        User user = citizen();

        com.residuosolido.app.model.Request existing = new com.residuosolido.app.model.Request();
        existing.setId("req1");
        existing.setContactUser(user);
        existing.restoreStatus(com.residuosolido.app.enums.RequestStatus.IN_PROGRESS);

        when(requestRepository.findById("req1"))
                .thenReturn(java.util.Optional.of(existing));

        assertThrows(IllegalStateException.class,
                () -> requestService.deleteOwnedRequest("req1", user));
    }

    @Test
    void rn11_deleteOwnedRequest_pending_deletesSuccessfully() {
        User user = citizen();

        com.residuosolido.app.model.Request existing = new com.residuosolido.app.model.Request();
        existing.setId("req1");
        existing.setContactUser(user);
        existing.restoreStatus(com.residuosolido.app.enums.RequestStatus.PENDING);

        when(requestRepository.findById("req1"))
                .thenReturn(java.util.Optional.of(existing));

        requestService.deleteOwnedRequest("req1", user);

        org.mockito.Mockito.verify(requestRepository).delete(existing);
    }
}
