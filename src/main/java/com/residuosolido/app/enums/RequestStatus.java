package com.residuosolido.app.enums;

/**
 * Estado de una solicitud de recolección con su máquina de estados embebida.
 * Implementa el patrón State: cada estado encapsula sus propias transiciones
 * válidas y las rechaza con la excepción correspondiente si son inválidas.
 *
 * Transiciones:
 *   PENDING     → IN_PROGRESS (accept), REJECTED (reject)
 *   IN_PROGRESS → COMPLETED (complete), REJECTED (reject)
 *   COMPLETED   → (terminal)
 *   REJECTED    → (terminal)
 */
public enum RequestStatus {

    PENDING {
        @Override public RequestStatus transitionAccept() { return IN_PROGRESS; }
        @Override public RequestStatus transitionComplete() { throw new IllegalStateException("error.request.complete_not_in_progress"); }
        @Override public RequestStatus transitionReject() { return REJECTED; }
        @Override public boolean canBeEdited() { return true; }
        @Override public boolean canBeDeleted() { return true; }
    },

    IN_PROGRESS {
        @Override public RequestStatus transitionAccept() { throw new IllegalStateException("error.request.accept_not_pending"); }
        @Override public RequestStatus transitionComplete() { return COMPLETED; }
        @Override public RequestStatus transitionReject() { return REJECTED; }
        @Override public boolean canBeEdited() { return false; }
        @Override public boolean canBeDeleted() { return false; }
    },

    COMPLETED {
        @Override public RequestStatus transitionAccept() { throw new IllegalStateException("error.request.accept_not_pending"); }
        @Override public RequestStatus transitionComplete() { throw new IllegalStateException("error.request.complete_not_in_progress"); }
        @Override public RequestStatus transitionReject() { throw new IllegalStateException("error.request.reject_invalid_state"); }
        @Override public boolean canBeEdited() { return false; }
        @Override public boolean canBeDeleted() { return false; }
    },

    REJECTED {
        @Override public RequestStatus transitionAccept() { throw new IllegalStateException("error.request.accept_not_pending"); }
        @Override public RequestStatus transitionComplete() { throw new IllegalStateException("error.request.complete_not_in_progress"); }
        @Override public RequestStatus transitionReject() { throw new IllegalStateException("error.request.reject_invalid_state"); }
        @Override public boolean canBeEdited() { return false; }
        @Override public boolean canBeDeleted() { return false; }
    };

    /**
     * Transición a IN_PROGRESS. Solo válida desde PENDING.
     * El caller es responsable de setear confirmedSlot antes de llamar.
     */
    public abstract RequestStatus transitionAccept();

    /**
     * Transición a COMPLETED. Solo válida desde IN_PROGRESS.
     */
    public abstract RequestStatus transitionComplete();

    /**
     * Transición a REJECTED. Válida desde PENDING e IN_PROGRESS.
     */
    public abstract RequestStatus transitionReject();

    /**
     * Si la solicitud puede ser editada por el ciudadano.
     */
    public abstract boolean canBeEdited();

    /**
     * Si la solicitud puede ser eliminada por el ciudadano.
     */
    public abstract boolean canBeDeleted();
}
