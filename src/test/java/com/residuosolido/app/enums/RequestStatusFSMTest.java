package com.residuosolido.app.enums;

import com.residuosolido.app.exception.StateException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verificación exhaustiva del FSM de RequestStatus.
 *
 * Máquina finita: 4 estados, 3 transiciones válidas totales.
 * Test: matriz de transiciones (16 intentos: 4 estados × 4 destinos).
 * Invariante 1: COMPLETED, REJECTED son terminales (no salidas).
 * Invariante 2: Cualquier transición inválida lanza StateException.
 */
@Tag("unit")
class RequestStatusFSMTest {

    @Test
    void testTerminalStates() {
        assertTrue(RequestStatus.COMPLETED.isTerminal());
        assertTrue(RequestStatus.REJECTED.isTerminal());
        assertFalse(RequestStatus.PENDING.isTerminal());
        assertFalse(RequestStatus.IN_PROGRESS.isTerminal());
    }

    @Test
    void testTransitionMatrix() {
        // PENDING: puede ir a IN_PROGRESS, REJECTED
        assertTrue(RequestStatus.PENDING.canTransitionTo(RequestStatus.IN_PROGRESS));
        assertTrue(RequestStatus.PENDING.canTransitionTo(RequestStatus.REJECTED));
        assertFalse(RequestStatus.PENDING.canTransitionTo(RequestStatus.COMPLETED));
        assertFalse(RequestStatus.PENDING.canTransitionTo(RequestStatus.PENDING));

        // IN_PROGRESS: puede ir a COMPLETED, REJECTED
        assertTrue(RequestStatus.IN_PROGRESS.canTransitionTo(RequestStatus.COMPLETED));
        assertTrue(RequestStatus.IN_PROGRESS.canTransitionTo(RequestStatus.REJECTED));
        assertFalse(RequestStatus.IN_PROGRESS.canTransitionTo(RequestStatus.PENDING));
        assertFalse(RequestStatus.IN_PROGRESS.canTransitionTo(RequestStatus.IN_PROGRESS));

        // COMPLETED: terminal (sin transiciones)
        assertFalse(RequestStatus.COMPLETED.canTransitionTo(RequestStatus.PENDING));
        assertFalse(RequestStatus.COMPLETED.canTransitionTo(RequestStatus.IN_PROGRESS));
        assertFalse(RequestStatus.COMPLETED.canTransitionTo(RequestStatus.COMPLETED));
        assertFalse(RequestStatus.COMPLETED.canTransitionTo(RequestStatus.REJECTED));

        // REJECTED: terminal (sin transiciones)
        assertFalse(RequestStatus.REJECTED.canTransitionTo(RequestStatus.PENDING));
        assertFalse(RequestStatus.REJECTED.canTransitionTo(RequestStatus.IN_PROGRESS));
        assertFalse(RequestStatus.REJECTED.canTransitionTo(RequestStatus.COMPLETED));
        assertFalse(RequestStatus.REJECTED.canTransitionTo(RequestStatus.REJECTED));
    }

    @Test
    void testAcceptTransition() {
        RequestStatus result = RequestStatus.PENDING.transitionAccept();
        assertEquals(RequestStatus.IN_PROGRESS, result);

        assertThrows(StateException.class, () -> RequestStatus.IN_PROGRESS.transitionAccept());
        assertThrows(StateException.class, () -> RequestStatus.COMPLETED.transitionAccept());
        assertThrows(StateException.class, () -> RequestStatus.REJECTED.transitionAccept());
    }

    @Test
    void testCompleteTransition() {
        RequestStatus result = RequestStatus.IN_PROGRESS.transitionComplete();
        assertEquals(RequestStatus.COMPLETED, result);

        assertThrows(StateException.class, () -> RequestStatus.PENDING.transitionComplete());
        assertThrows(StateException.class, () -> RequestStatus.COMPLETED.transitionComplete());
        assertThrows(StateException.class, () -> RequestStatus.REJECTED.transitionComplete());
    }

    @Test
    void testRejectTransition() {
        RequestStatus result1 = RequestStatus.PENDING.transitionReject();
        assertEquals(RequestStatus.REJECTED, result1);

        RequestStatus result2 = RequestStatus.IN_PROGRESS.transitionReject();
        assertEquals(RequestStatus.REJECTED, result2);

        assertThrows(StateException.class, () -> RequestStatus.COMPLETED.transitionReject());
        assertThrows(StateException.class, () -> RequestStatus.REJECTED.transitionReject());
    }

    @Test
    void testEditableStates() {
        assertTrue(RequestStatus.PENDING.canBeEdited());
        assertFalse(RequestStatus.IN_PROGRESS.canBeEdited());
        assertFalse(RequestStatus.COMPLETED.canBeEdited());
        assertFalse(RequestStatus.REJECTED.canBeEdited());
    }

    @Test
    void testDeletableStates() {
        assertTrue(RequestStatus.PENDING.canBeDeleted());
        assertFalse(RequestStatus.IN_PROGRESS.canBeDeleted());
        assertFalse(RequestStatus.COMPLETED.canBeDeleted());
        assertFalse(RequestStatus.REJECTED.canBeDeleted());
    }
}
