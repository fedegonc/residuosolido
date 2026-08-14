package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.RequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestOrgServiceTest {

    private RequestRepository requestRepository;
    private RequestOrgService service;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        service = new RequestOrgService(requestRepository);
    }

    private User org(String id) {
        User u = new User();
        u.setId(id);
        u.setRole(Role.ORGANIZATION);
        return u;
    }

    private Request request(String id, User org, RequestStatus status) {
        Request r = new Request();
        r.setId(id);
        r.setStatus(status);
        r.assignOrganization(org);
        return r;
    }

    @Test
    void getOwnedOrgRequest_notFound_throws() {
        when(requestRepository.findById("r1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getOwnedOrgRequest("r1", org("org1")));
    }

    @Test
    void getOwnedOrgRequest_notOwned_throwsSecurityException() {
        Request r = request("r1", org("otherOrg"), RequestStatus.PENDING);
        when(requestRepository.findById("r1")).thenReturn(Optional.of(r));
        assertThrows(SecurityException.class, () -> service.getOwnedOrgRequest("r1", org("org1")));
    }

    @Test
    void getOwnedOrgRequest_owned_returnsRequest() {
        User organization = org("org1");
        Request r = request("r1", organization, RequestStatus.PENDING);
        when(requestRepository.findById("r1")).thenReturn(Optional.of(r));
        assertEquals(r, service.getOwnedOrgRequest("r1", organization));
    }

    @Test
    void getRequestsByOrganization_delegatesToRepository() {
        User organization = org("org1");
        List<Request> expected = List.of(request("r1", organization, RequestStatus.PENDING));
        when(requestRepository.findByOrganizationOrderByCreatedAtDesc(eq(organization), any(PageRequest.class)))
                .thenReturn(expected);
        assertEquals(expected, service.getRequestsByOrganization(organization, 0, 20));
    }

    @Test
    void getOrgRequestsByStatusFilter_blankStatus_returnsAllRequests() {
        User organization = org("org1");
        List<Request> expected = List.of(request("r1", organization, RequestStatus.PENDING));
        when(requestRepository.findByOrganizationOrderByCreatedAtDesc(eq(organization), any(PageRequest.class)))
                .thenReturn(expected);
        assertEquals(expected, service.getOrgRequestsByStatusFilter(organization, "  ", 0, 20));
    }

    @Test
    void getOrgRequestsByStatusFilter_validStatus_filtersRequests() {
        User organization = org("org1");
        List<Request> expected = List.of(request("r1", organization, RequestStatus.IN_PROGRESS));
        when(requestRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(
                eq(organization), eq(RequestStatus.IN_PROGRESS), any(PageRequest.class)))
                .thenReturn(expected);
        assertEquals(expected, service.getOrgRequestsByStatusFilter(organization, "in_progress", 0, 20));
    }

    @Test
    void getOrgRequestsByStatusFilter_invalidStatus_fallsBackToAllRequests() {
        User organization = org("org1");
        List<Request> expected = List.of(request("r1", organization, RequestStatus.PENDING));
        when(requestRepository.findByOrganizationOrderByCreatedAtDesc(eq(organization), any(PageRequest.class)))
                .thenReturn(expected);
        assertEquals(expected, service.getOrgRequestsByStatusFilter(organization, "NOT_A_STATUS", 0, 20));
    }

    @Test
    void getRecentPendingRequestsByOrganization_delegatesToRepository() {
        User organization = org("org1");
        List<Request> expected = List.of(request("r1", organization, RequestStatus.PENDING));
        when(requestRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(
                eq(organization), eq(RequestStatus.PENDING), any(PageRequest.class)))
                .thenReturn(expected);
        assertEquals(expected, service.getRecentPendingRequestsByOrganization(organization, 5));
    }
}
