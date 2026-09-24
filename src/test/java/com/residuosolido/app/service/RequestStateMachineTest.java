package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.StateException;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RequestStateMachine: transiciones de estado")
class RequestStateMachineTest {

    private RequestStateMachine sm;

    @BeforeEach
    void setUp() {
        sm = new RequestStateMachine();
    }

    // ========== accept() ==========

    @Test
    @DisplayName("accept: PENDING + slot válido → IN_PROGRESS")
    void acceptValidPending() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.PENDING);

        assertDoesNotThrow(() -> sm.accept(req, TimeSlot.MANANA));
        assertEquals(RequestStatus.IN_PROGRESS, req.getStatus());
        assertEquals(TimeSlot.MANANA, req.getConfirmedSlot());
    }

    @Test
    @DisplayName("accept: PENDING + slot nulo → error")
    void acceptNullSlot() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.PENDING);

        ValidationException ex = assertThrows(ValidationException.class, () -> sm.accept(req, null));
        assertEquals(ServerMessage.ERROR_REQUEST_SLOT_REQUIRED, ex.key());
    }

    @Test
    @DisplayName("accept: IN_PROGRESS → error")
    void acceptInProgress() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.IN_PROGRESS);

        StateException ex = assertThrows(StateException.class, () -> sm.accept(req, TimeSlot.TARDE));
        assertEquals(ServerMessage.ERROR_REQUEST_ACCEPT_NOT_PENDING, ex.key());
    }

    @Test
    @DisplayName("accept: COMPLETED → error")
    void acceptCompleted() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.COMPLETED);

        StateException ex = assertThrows(StateException.class, () -> sm.accept(req, TimeSlot.NOCHE));
        assertEquals(ServerMessage.ERROR_REQUEST_ACCEPT_NOT_PENDING, ex.key());
    }

    // ========== complete() ==========

    @Test
    @DisplayName("complete: IN_PROGRESS → COMPLETED")
    void completeInProgress() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.IN_PROGRESS);

        assertDoesNotThrow(() -> sm.complete(req));
        assertEquals(RequestStatus.COMPLETED, req.getStatus());
    }

    @Test
    @DisplayName("complete: PENDING → error")
    void completePending() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.PENDING);

        StateException ex = assertThrows(StateException.class, () -> sm.complete(req));
        assertEquals(ServerMessage.ERROR_REQUEST_COMPLETE_NOT_IN_PROGRESS, ex.key());
    }

    @Test
    @DisplayName("complete: COMPLETED (terminal) → error")
    void completeAlreadyCompleted() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.COMPLETED);

        StateException ex = assertThrows(StateException.class, () -> sm.complete(req));
        assertEquals(ServerMessage.ERROR_REQUEST_COMPLETE_NOT_IN_PROGRESS, ex.key());
    }

    // ========== reject() ==========

    @Test
    @DisplayName("reject: PENDING → REJECTED")
    void rejectPending() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.PENDING);

        assertDoesNotThrow(() -> sm.reject(req));
        assertEquals(RequestStatus.REJECTED, req.getStatus());
    }

    @Test
    @DisplayName("reject: IN_PROGRESS → REJECTED")
    void rejectInProgress() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.IN_PROGRESS);

        assertDoesNotThrow(() -> sm.reject(req));
        assertEquals(RequestStatus.REJECTED, req.getStatus());
    }

    @Test
    @DisplayName("reject: COMPLETED (terminal) → error")
    void rejectCompleted() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.COMPLETED);

        StateException ex = assertThrows(StateException.class, () -> sm.reject(req));
        assertEquals(ServerMessage.ERROR_REQUEST_REJECT_INVALID_STATE, ex.key());
    }

    @Test
    @DisplayName("reject: REJECTED (terminal) → error")
    void rejectAlreadyRejected() {
        Request req = new Request();
        req.restoreStatus(RequestStatus.REJECTED);

        StateException ex = assertThrows(StateException.class, () -> sm.reject(req));
        assertEquals(ServerMessage.ERROR_REQUEST_REJECT_INVALID_STATE, ex.key());
    }

    // ========== Queries ==========

    @Test
    @DisplayName("canEdit: solo PENDING")
    void canEditQuery() {
        Request pending = new Request();
        pending.restoreStatus(RequestStatus.PENDING);
        assertTrue(sm.canEdit(pending));

        Request inProgress = new Request();
        inProgress.restoreStatus(RequestStatus.IN_PROGRESS);
        assertFalse(sm.canEdit(inProgress));

        Request completed = new Request();
        completed.restoreStatus(RequestStatus.COMPLETED);
        assertFalse(sm.canEdit(completed));
    }

    @Test
    @DisplayName("canDelete: solo PENDING")
    void canDeleteQuery() {
        Request pending = new Request();
        pending.restoreStatus(RequestStatus.PENDING);
        assertTrue(sm.canDelete(pending));

        Request inProgress = new Request();
        inProgress.restoreStatus(RequestStatus.IN_PROGRESS);
        assertFalse(sm.canDelete(inProgress));
    }

    @Test
    @DisplayName("canAccept: solo PENDING")
    void canAcceptQuery() {
        Request pending = new Request();
        pending.restoreStatus(RequestStatus.PENDING);
        assertTrue(sm.canAccept(pending));

        Request inProgress = new Request();
        inProgress.restoreStatus(RequestStatus.IN_PROGRESS);
        assertFalse(sm.canAccept(inProgress));
    }

    @Test
    @DisplayName("canComplete: solo IN_PROGRESS")
    void canCompleteQuery() {
        Request pending = new Request();
        pending.restoreStatus(RequestStatus.PENDING);
        assertFalse(sm.canComplete(pending));

        Request inProgress = new Request();
        inProgress.restoreStatus(RequestStatus.IN_PROGRESS);
        assertTrue(sm.canComplete(inProgress));

        Request completed = new Request();
        completed.restoreStatus(RequestStatus.COMPLETED);
        assertFalse(sm.canComplete(completed));
    }

    @Test
    @DisplayName("canReject: PENDING e IN_PROGRESS, no COMPLETED/REJECTED")
    void canRejectQuery() {
        Request pending = new Request();
        pending.restoreStatus(RequestStatus.PENDING);
        assertTrue(sm.canReject(pending));

        Request inProgress = new Request();
        inProgress.restoreStatus(RequestStatus.IN_PROGRESS);
        assertTrue(sm.canReject(inProgress));

        Request completed = new Request();
        completed.restoreStatus(RequestStatus.COMPLETED);
        assertFalse(sm.canReject(completed));

        Request rejected = new Request();
        rejected.restoreStatus(RequestStatus.REJECTED);
        assertFalse(sm.canReject(rejected));
    }
}
