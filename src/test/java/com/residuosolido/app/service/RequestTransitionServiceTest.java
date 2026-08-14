package com.residuosolido.app.service;

import com.residuosolido.app.enums.*;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RequestTransitionServiceTest {

    private RequestRepository repo;
    private NotificationService notif;
    private RequestOrgService orgSvc;
    private RequestTransitionService svc;
    private User org;

    @BeforeEach
    void setUp() {
        repo = mock(RequestRepository.class);
        notif = mock(NotificationService.class);
        orgSvc = mock(RequestOrgService.class);
        svc = new RequestTransitionService(repo, notif, orgSvc);
        org = new User();
        org.setId("org1");
        org.setRole(Role.ORGANIZATION);
        org.setFirstName("Coop");
        org.setPhone("+59899123456");
        when(notif.isEnabled()).thenReturn(true);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private Request req(RequestStatus st) {
        Request r = new Request();
        r.setId("r1");
        r.setOrganization(org);
        r.setStatus(st);
        r.setGuestName("Juan");
        r.setGuestPhone("+59899876543");
        r.setMaterials(List.of(MaterialCategory.PLASTICO));
        r.setCity(City.RIVERA);
        r.setAddress("Calle 1");
        return r;
    }

    @Test void accept_pending_setsInProgress() {
        Request r = req(RequestStatus.PENDING);
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(r);
        svc.acceptRequest("r1", org, TimeSlot.MANANA);
        assertEquals(RequestStatus.IN_PROGRESS, r.getStatus());
        assertEquals(TimeSlot.MANANA, r.getConfirmedSlot());
        verify(repo).save(r);
    }

    @Test void accept_completed_throws() {
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(req(RequestStatus.COMPLETED));
        assertThrows(IllegalStateException.class,
                () -> svc.acceptRequest("r1", org, TimeSlot.MANANA));
        verify(repo, never()).save(any());
    }

    @Test void accept_nullSlot_throws() {
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(req(RequestStatus.PENDING));
        assertThrows(IllegalArgumentException.class,
                () -> svc.acceptRequest("r1", org, null));
        verify(repo, never()).save(any());
    }

    @Test void reject_pending_setsRejected() {
        Request r = req(RequestStatus.PENDING);
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(r);
        svc.rejectRequest("r1", org);
        assertEquals(RequestStatus.REJECTED, r.getStatus());
        verify(repo).save(r);
    }

    @Test void reject_inProgress_setsRejected() {
        Request r = req(RequestStatus.IN_PROGRESS);
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(r);
        svc.rejectRequest("r1", org);
        assertEquals(RequestStatus.REJECTED, r.getStatus());
    }

    @Test void reject_completed_throws() {
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(req(RequestStatus.COMPLETED));
        assertThrows(IllegalStateException.class, () -> svc.rejectRequest("r1", org));
        verify(repo, never()).save(any());
    }

    @Test void complete_inProgress_setsCompleted() {
        Request r = req(RequestStatus.IN_PROGRESS);
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(r);
        svc.completeRequest("r1", org);
        assertEquals(RequestStatus.COMPLETED, r.getStatus());
        verify(repo).save(r);
    }

    @Test void complete_pending_throws() {
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(req(RequestStatus.PENDING));
        assertThrows(IllegalStateException.class, () -> svc.completeRequest("r1", org));
        verify(repo, never()).save(any());
    }

    @Test void accept_concurrent_throwsIllegalState() {
        Request r = req(RequestStatus.PENDING);
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(r);
        when(repo.save(any())).thenThrow(new OptimisticLockingFailureException("race"));
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> svc.acceptRequest("r1", org, TimeSlot.MANANA));
        assertEquals("flash.request.concurrent_modification", ex.getMessage());
    }

    @Test void complete_concurrent_throwsIllegalState() {
        Request r = req(RequestStatus.IN_PROGRESS);
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(r);
        when(repo.save(any())).thenThrow(new OptimisticLockingFailureException("race"));
        assertThrows(IllegalStateException.class, () -> svc.completeRequest("r1", org));
    }

    @Test void accept_sendsNotification() {
        Request r = req(RequestStatus.PENDING);
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(r);
        svc.acceptRequest("r1", org, TimeSlot.MANANA);
        verify(notif).sendWhatsApp(eq("+59899876543"), contains("aceptada"));
    }

    @Test void complete_sendsNotification() {
        Request r = req(RequestStatus.IN_PROGRESS);
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(r);
        svc.completeRequest("r1", org);
        verify(notif).sendWhatsApp(eq("+59899876543"), contains("completada"));
    }

    @Test void noPhone_noNotification() {
        Request r = req(RequestStatus.PENDING);
        r.setGuestPhone(null);
        when(orgSvc.getOwnedOrgRequest("r1", org)).thenReturn(r);
        svc.rejectRequest("r1", org);
        verify(notif, never()).sendWhatsApp(any(), any());
    }
}
