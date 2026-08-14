package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
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

class RequestQueryServiceTest {

    private RequestRepository requestRepository;
    private RequestQueryService service;

    @BeforeEach
    void setUp() {
        requestRepository = mock(RequestRepository.class);
        service = new RequestQueryService(requestRepository);
    }

    private User user(String id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private Request request(String id, User owner, RequestStatus status) {
        Request r = new Request();
        r.setId(id);
        r.setUser(owner);
        r.setStatus(status);
        return r;
    }

    @Test
    void getRequestsByUser_delegatesToRepositoryWithPaging() {
        User u = user("u1");
        List<Request> expected = List.of(request("r1", u, RequestStatus.PENDING));
        when(requestRepository.findByUser(eq(u), any(PageRequest.class))).thenReturn(expected);
        assertEquals(expected, service.getRequestsByUser(u, 0, 20));
    }

    @Test
    void getOwnedRequest_notFound_throws() {
        when(requestRepository.findById("r1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getOwnedRequest("r1", user("u1")));
    }

    @Test
    void getOwnedRequest_notOwned_throwsSecurityException() {
        Request r = request("r1", user("otherUser"), RequestStatus.PENDING);
        when(requestRepository.findById("r1")).thenReturn(Optional.of(r));
        assertThrows(SecurityException.class, () -> service.getOwnedRequest("r1", user("u1")));
    }

    @Test
    void getOwnedRequest_guestRequestWithNoUser_throwsSecurityException() {
        Request r = request("r1", null, RequestStatus.PENDING);
        when(requestRepository.findById("r1")).thenReturn(Optional.of(r));
        assertThrows(SecurityException.class, () -> service.getOwnedRequest("r1", user("u1")));
    }

    @Test
    void getOwnedRequest_owned_returnsRequest() {
        User u = user("u1");
        Request r = request("r1", u, RequestStatus.PENDING);
        when(requestRepository.findById("r1")).thenReturn(Optional.of(r));
        assertEquals(r, service.getOwnedRequest("r1", u));
    }

    @Test
    void getEditableOwnedRequest_notEditable_throws() {
        User u = user("u1");
        Request r = request("r1", u, RequestStatus.COMPLETED);
        when(requestRepository.findById("r1")).thenReturn(Optional.of(r));
        assertThrows(IllegalStateException.class, () -> service.getEditableOwnedRequest("r1", u));
    }

    @Test
    void getEditableOwnedRequest_pending_returnsRequest() {
        User u = user("u1");
        Request r = request("r1", u, RequestStatus.PENDING);
        when(requestRepository.findById("r1")).thenReturn(Optional.of(r));
        assertEquals(r, service.getEditableOwnedRequest("r1", u));
    }

    @Test
    void getGuestRequestsByPhone_blankPhone_returnsEmptyList() {
        assertTrue(service.getGuestRequestsByPhone("  ").isEmpty());
        assertTrue(service.getGuestRequestsByPhone(null).isEmpty());
    }

    @Test
    void getGuestRequestsByPhone_delegatesToRepository() {
        Request r = request("r1", null, RequestStatus.PENDING);
        when(requestRepository.findByGuestPhoneOrderByCreatedAtDesc("+59899123456"))
                .thenReturn(List.of(r));
        assertEquals(List.of(r), service.getGuestRequestsByPhone(" +59899123456 "));
    }
}
