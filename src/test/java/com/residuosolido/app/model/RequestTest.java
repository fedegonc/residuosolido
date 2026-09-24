package com.residuosolido.app.model;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.exception.StateException;
import com.residuosolido.app.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Request: máquina de estados")
class RequestTest {

    private TimeSlot validSlot() {
        return TimeSlot.MANANA;
    }

    @Test
    @DisplayName("PENDING puede transicionar a IN_PROGRESS")
    void pendingCanTransitionToInProgress() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.PENDING);

        assertDoesNotThrow(() -> req.accept(validSlot()));
        assertEquals(RequestStatus.IN_PROGRESS, req.getStatus());
    }

    @Test
    @DisplayName("PENDING puede transicionar a REJECTED")
    void pendingCanTransitionToRejected() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.PENDING);

        assertDoesNotThrow(() -> req.reject());
        assertEquals(RequestStatus.REJECTED, req.getStatus());
    }

    @Test
    @DisplayName("IN_PROGRESS puede transicionar a COMPLETED")
    void inProgressCanTransitionToCompleted() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.IN_PROGRESS);

        assertDoesNotThrow(() -> req.complete());
        assertEquals(RequestStatus.COMPLETED, req.getStatus());
    }

    @Test
    @DisplayName("IN_PROGRESS puede transicionar a REJECTED")
    void inProgressCanTransitionToRejected() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.IN_PROGRESS);

        assertDoesNotThrow(() -> req.reject());
        assertEquals(RequestStatus.REJECTED, req.getStatus());
    }

    @Test
    @DisplayName("REJECTED es terminal (no puede volver a PENDING)")
    void rejectedIsTerminalForAccept() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.REJECTED);

        assertThrows(StateException.class, () -> req.accept(validSlot()));
    }

    @Test
    @DisplayName("REJECTED es terminal (no puede ir a COMPLETED)")
    void rejectedIsTerminalForComplete() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.REJECTED);

        assertThrows(StateException.class, () -> req.complete());
    }

    @Test
    @DisplayName("COMPLETED es terminal")
    void completedIsTerminal() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.COMPLETED);

        assertThrows(StateException.class, () -> req.accept(validSlot()));
        assertThrows(StateException.class, () -> req.reject());
        assertThrows(StateException.class, () -> req.complete());
    }

    @Test
    @DisplayName("accept() requiere TimeSlot no nulo")
    void acceptRequiresSlot() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.PENDING);

        assertThrows(ValidationException.class, () -> req.accept(null));
    }

    @Test
    @DisplayName("accept() establece confirmedSlot")
    void acceptSetsSlot() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.PENDING);
        TimeSlot slot = TimeSlot.TARDE;

        req.accept(slot);

        assertEquals(slot, req.getConfirmedSlot());
    }

    @Test
    @DisplayName("Factory forCitizen() arranca en PENDING")
    void factoryForCitizenStartsPending() {
        User user = new User();
        Request req = Request.forCitizen(user);

        assertEquals(RequestStatus.PENDING, req.getStatus());
        assertNotNull(req.getCreatedAt());
    }

    @Test
    @DisplayName("Factory forGuest() arranca en PENDING")
    void factoryForGuestStartsPending() {
        Request req = Request.forGuest("Juan", "098123456", "ABC123");

        assertEquals(RequestStatus.PENDING, req.getStatus());
        assertEquals("Juan", req.getGuestName());
        assertEquals("098123456", req.getGuestPhone());
        assertEquals("ABC123", req.getTrackingCode());
    }
}
