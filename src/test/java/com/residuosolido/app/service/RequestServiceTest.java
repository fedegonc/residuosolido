package com.residuosolido.app.service;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.exception.OwnershipException;
import com.residuosolido.app.exception.StateException;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Cubre las ramas de RequestService no ejercitadas por RequestServiceValidationTest
 * (que se enfoca en las validaciones RN-10/RN-11): ownership, transiciones de estado,
 * concurrencia optimista y consultas de invitado/organización.
 */
@Tag("unit")
class RequestServiceTest {

    private RequestRepository requestRepository;
    private LocalImageService imageService;
    private CityOrgService cityOrgService;
    private RequestService requestService;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        imageService = mock(LocalImageService.class);
        cityOrgService = mock(CityOrgService.class);
        requestService = new RequestService(requestRepository, imageService, cityOrgService);
    }

    private User citizen(String id) {
        return TestFixtures.citizen(id, "+59899123456");
    }

    private User org(String id) {
        return TestFixtures.organization(id, City.RIVERA, MaterialCategory.PLASTICO);
    }

    private Request requestOf(User owner, RequestStatus status) {
        Request r = new Request();
        r.setId("req1");
        r.setContactUser(owner);
        r.restoreStatus(status);
        return r;
    }

    private Request orgRequestOf(User org, RequestStatus status) {
        Request r = new Request();
        r.setId("req1");
        r.assignOrganization(org);
        r.restoreStatus(status);
        return r;
    }

    // ───────────────────── getOwnedRequest ─────────────────────

    @Test
    void getOwnedRequest_notFound_throwsValidationException() {
        when(requestRepository.findById("req1")).thenReturn(Optional.empty());
        assertThrows(ValidationException.class,
                () -> requestService.getOwnedRequest("req1", citizen("u1")));
    }

    @Test
    void getOwnedRequest_belongsToOtherUser_throwsOwnershipException() {
        Request existing = requestOf(citizen("owner"), RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        assertThrows(OwnershipException.class,
                () -> requestService.getOwnedRequest("req1", citizen("intruder")));
    }

    @Test
    void getOwnedRequest_guestRequest_throwsOwnershipException() {
        Request existing = new Request();
        existing.setId("req1");
        existing.setGuestContact("Juan", null, null);
        existing.restoreStatus(RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        assertThrows(OwnershipException.class,
                () -> requestService.getOwnedRequest("req1", citizen("u1")));
    }

    @Test
    void getOwnedRequest_owner_returnsRequest() {
        User owner = citizen("u1");
        Request existing = requestOf(owner, RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        assertEquals(existing, requestService.getOwnedRequest("req1", owner));
    }

    // ─────────────────── getEditableOwnedRequest ───────────────────

    @Test
    void getEditableOwnedRequest_notPending_throwsStateException() {
        User owner = citizen("u1");
        Request existing = requestOf(owner, RequestStatus.IN_PROGRESS);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        assertThrows(StateException.class,
                () -> requestService.getEditableOwnedRequest("req1", owner));
    }

    @Test
    void getEditableOwnedRequest_pending_returnsRequest() {
        User owner = citizen("u1");
        Request existing = requestOf(owner, RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        assertEquals(existing, requestService.getEditableOwnedRequest("req1", owner));
    }

    // ───────────────────── getOwnedOrgRequest ─────────────────────

    @Test
    void getOwnedOrgRequest_notFound_throwsValidationException() {
        when(requestRepository.findById("req1")).thenReturn(Optional.empty());
        assertThrows(ValidationException.class,
                () -> requestService.getOwnedOrgRequest("req1", org("org1")));
    }

    @Test
    void getOwnedOrgRequest_belongsToOtherOrg_throwsOwnershipException() {
        Request existing = orgRequestOf(org("org1"), RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        assertThrows(OwnershipException.class,
                () -> requestService.getOwnedOrgRequest("req1", org("org2")));
    }

    @Test
    void getOwnedOrgRequest_unassigned_throwsOwnershipException() {
        Request existing = new Request();
        existing.setId("req1");
        existing.restoreStatus(RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        assertThrows(OwnershipException.class,
                () -> requestService.getOwnedOrgRequest("req1", org("org1")));
    }

    // ───────────────────── transiciones de estado ─────────────────────

    @Test
    void acceptRequest_pending_transitionsToInProgress() {
        User organization = org("org1");
        Request existing = orgRequestOf(organization, RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        requestService.acceptRequest("req1", organization, TimeSlot.MANANA);

        assertEquals(RequestStatus.IN_PROGRESS, existing.getStatus());
        assertEquals(TimeSlot.MANANA, existing.getConfirmedSlot());
        verify(requestRepository).save(existing);
    }

    @Test
    void acceptRequest_alreadyInProgress_throwsStateException() {
        User organization = org("org1");
        Request existing = orgRequestOf(organization, RequestStatus.IN_PROGRESS);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));

        assertThrows(StateException.class,
                () -> requestService.acceptRequest("req1", organization, TimeSlot.MANANA));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void acceptRequest_concurrentModification_throwsStateException() {
        User organization = org("org1");
        Request existing = orgRequestOf(organization, RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        when(requestRepository.save(any(Request.class)))
                .thenThrow(new OptimisticLockingFailureException("stale version"));

        assertThrows(StateException.class,
                () -> requestService.acceptRequest("req1", organization, TimeSlot.MANANA));
    }

    @Test
    void rejectRequest_pending_transitionsToRejected() {
        User organization = org("org1");
        Request existing = orgRequestOf(organization, RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        requestService.rejectRequest("req1", organization);

        assertEquals(RequestStatus.REJECTED, existing.getStatus());
    }

    @Test
    void rejectRequest_alreadyTerminal_throwsStateException() {
        User organization = org("org1");
        Request existing = orgRequestOf(organization, RequestStatus.COMPLETED);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));

        assertThrows(StateException.class,
                () -> requestService.rejectRequest("req1", organization));
    }

    @Test
    void completeRequest_inProgress_transitionsToCompleted() {
        User organization = org("org1");
        Request existing = orgRequestOf(organization, RequestStatus.IN_PROGRESS);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        requestService.completeRequest("req1", organization);

        assertEquals(RequestStatus.COMPLETED, existing.getStatus());
    }

    @Test
    void completeRequest_stillPending_throwsStateException() {
        User organization = org("org1");
        Request existing = orgRequestOf(organization, RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));

        assertThrows(StateException.class,
                () -> requestService.completeRequest("req1", organization));
    }

    // ───────────────────── deleteOwnedRequest ─────────────────────

    @Test
    void deleteOwnedRequest_concurrentModification_throwsStateException() {
        User owner = citizen("u1");
        Request existing = requestOf(owner, RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        doThrow(new OptimisticLockingFailureException("stale version"))
                .when(requestRepository).delete(existing);

        assertThrows(StateException.class,
                () -> requestService.deleteOwnedRequest("req1", owner));
    }

    // ───────────────────── getGuestRequests ─────────────────────

    @Test
    void getGuestRequests_blankPhone_returnsEmptyWithoutQuerying() {
        assertTrue(requestService.getGuestRequests("  ", "CODE1234").isEmpty());
        verifyNoInteractions(requestRepository);
    }

    @Test
    void getGuestRequests_blankTrackingCode_returnsEmptyWithoutQuerying() {
        assertTrue(requestService.getGuestRequests("+59899123456", " ").isEmpty());
        verifyNoInteractions(requestRepository);
    }

    @Test
    void getGuestRequests_malformedPhone_returnsEmptyInsteadOfThrowing() {
        // Bug real: un "+" sin codificar en la URL de redirect llega acá como espacio
        // (" 59892224955") -> PhoneNumber.normalize tiraba ValidationException, que
        // GlobalExceptionHandler mandaba a /entrar (mal, el invitado nunca inicio sesion).
        assertTrue(requestService.getGuestRequests(" 59892224955", "CODE1234").isEmpty());
        verifyNoInteractions(requestRepository);
    }

    @Test
    void getGuestRequests_valid_normalizesPhoneAndQueries() {
        Request found = new Request();
        when(requestRepository.findByGuestPhoneAndTrackingCodeOrderByCreatedAtDesc("+59899123456", "CODE1234"))
                .thenReturn(List.of(found));

        List<Request> result = requestService.getGuestRequests(" +598 99 123 456 ", " CODE1234 ");

        assertEquals(List.of(found), result);
        verify(requestRepository).findByGuestPhoneAndTrackingCodeOrderByCreatedAtDesc("+59899123456", "CODE1234");
    }

    // ─────────────────── getOrgRequestsByStatusFilter ───────────────────

    @Test
    void getOrgRequestsByStatusFilter_blankStatus_delegatesToUnfiltered() {
        User organization = org("org1");
        requestService.getOrgRequestsByStatusFilter(organization, "  ", 0, 10);
        verify(requestRepository).findByOrganizationOrderByCreatedAtDesc(eq(organization), any());
        verify(requestRepository, never()).findByOrganizationAndStatusOrderByCreatedAtDesc(any(), any(), any());
    }

    @Test
    void getOrgRequestsByStatusFilter_validStatus_filtersByStatus() {
        User organization = org("org1");
        requestService.getOrgRequestsByStatusFilter(organization, "pending", 0, 10);
        verify(requestRepository)
                .findByOrganizationAndStatusOrderByCreatedAtDesc(eq(organization), eq(RequestStatus.PENDING), any());
    }

    @Test
    void getOrgRequestsByStatusFilter_invalidStatus_fallsBackToUnfiltered() {
        User organization = org("org1");
        requestService.getOrgRequestsByStatusFilter(organization, "no-existe", 0, 10);
        verify(requestRepository).findByOrganizationOrderByCreatedAtDesc(eq(organization), any());
        verify(requestRepository, never()).findByOrganizationAndStatusOrderByCreatedAtDesc(any(), any(), any());
    }

    // ───────────────────── validateGuest ─────────────────────

    @Test
    void createRequest_guestNameTooLong_throwsValidationException() {
        String longName = "a".repeat(101);
        ValidationException ex = assertThrows(ValidationException.class, () -> requestService.createRequest(
                null, City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO), longName, "+59899123456", "org1"));
        assertNotNull(ex);
    }

    @Test
    void createRequest_guestInvalidPhone_throwsValidationException() {
        assertThrows(ValidationException.class, () -> requestService.createRequest(
                null, City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO), "Juan", "123", "org1"));
    }

    // ───────────────────── validateMaterials ─────────────────────

    @Test
    void createRequest_organizationWithoutAcceptedMaterials_throwsValidationException() {
        User citizen = citizen("u1");
        User organization = org("org1");
        organization.setAcceptedMaterials(null);
        when(cityOrgService.findOrganizationByIdAndCity("org1", City.RIVERA)).thenReturn(organization);

        assertThrows(ValidationException.class, () -> requestService.createRequest(
                citizen, City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO), null, null, "org1"));
    }

    // ───────────────────── createRequestWithImage ─────────────────────

    @Test
    void createRequestWithImage_validatesImageBeforeCreating() {
        MockMultipartFile file = new MockMultipartFile("imageFile", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
        doThrow(new ValidationException(com.residuosolido.app.enums.ServerMessage.ERROR_IMAGE_TOO_LARGE))
                .when(imageService).validateImage(file);

        assertThrows(ValidationException.class, () -> requestService.createRequestWithImage(
                citizen("u1"), City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO), null, null, "org1", file));
        verifyNoInteractions(requestRepository);
    }

    @Test
    void createRequestWithImage_validFile_attachesImageAfterCreate() {
        User citizen = citizen("u1");
        User organization = org("org1");
        MockMultipartFile file = new MockMultipartFile("imageFile", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(cityOrgService.findOrganizationByIdAndCity("org1", City.RIVERA)).thenReturn(organization);
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));
        Request withImage = new Request();
        withImage.setImageUrl("uploads/photo.jpg");
        when(imageService.attachImageToRequest(any(Request.class), eq(file))).thenReturn(withImage);

        Request result = requestService.createRequestWithImage(citizen, City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO), null, null, "org1", file);

        assertEquals("uploads/photo.jpg", result.getImageUrl());
        verify(imageService).attachImageToRequest(any(Request.class), eq(file));
    }

    @Test
    void createRequestWithImage_emptyFile_doesNotAttach() {
        User citizen = citizen("u1");
        User organization = org("org1");
        MockMultipartFile emptyFile = new MockMultipartFile("imageFile", "", "image/jpeg", new byte[0]);
        when(cityOrgService.findOrganizationByIdAndCity("org1", City.RIVERA)).thenReturn(organization);
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        requestService.createRequestWithImage(citizen, City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO), null, null, "org1", emptyFile);

        verify(imageService, never()).attachImageToRequest(any(), any());
    }

    // ───────────────────── updateRequest ─────────────────────

    @Test
    void updateRequest_notOwner_throwsOwnershipException() {
        Request existing = requestOf(citizen("owner"), RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));

        assertThrows(OwnershipException.class, () -> requestService.updateRequest(
                "req1", citizen("intruder"), City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO), "org1", null));
    }

    @Test
    void updateRequest_notPending_throwsStateException() {
        User owner = citizen("u1");
        Request existing = requestOf(owner, RequestStatus.IN_PROGRESS);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));

        assertThrows(StateException.class, () -> requestService.updateRequest(
                "req1", owner, City.RIVERA, "Calle 123", null,
                List.of(MaterialCategory.PLASTICO), "org1", null));
    }

    @Test
    void updateRequest_valid_updatesFieldsAndReassignsOrganization() {
        User owner = citizen("u1");
        Request existing = requestOf(owner, RequestStatus.PENDING);
        User newOrg = org("org2");
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        when(cityOrgService.findOrganizationByIdAndCity("org2", City.RIVERA)).thenReturn(newOrg);
        when(requestRepository.save(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));
        when(imageService.attachImageToRequest(any(Request.class), any())).thenAnswer(inv -> inv.getArgument(0));

        Request result = requestService.updateRequest("req1", owner, City.RIVERA, "Nueva calle", "Ref",
                List.of(MaterialCategory.PLASTICO), "org2", null);

        assertEquals("Nueva calle", result.getAddress());
        assertEquals(newOrg, result.getOrganization());
        verify(requestRepository).save(existing);
    }

    @Test
    void updateRequest_concurrentModification_throwsStateException() {
        User owner = citizen("u1");
        Request existing = requestOf(owner, RequestStatus.PENDING);
        when(requestRepository.findById("req1")).thenReturn(Optional.of(existing));
        when(cityOrgService.findOrganizationByIdAndCity("org1", City.RIVERA)).thenReturn(org("org1"));
        when(requestRepository.save(any(Request.class)))
                .thenThrow(new OptimisticLockingFailureException("stale version"));

        assertThrows(StateException.class, () -> requestService.updateRequest(
                "req1", owner, City.RIVERA, "Nueva calle", null,
                List.of(MaterialCategory.PLASTICO), "org1", null));
    }
}
