package com.residuosolido.app.enums;

/**
 * Estado de una solicitud de recolección.
 *
 * Transiciones:
 *   PENDING     → IN_PROGRESS (accept), REJECTED (reject)
 *   IN_PROGRESS → COMPLETED (complete), REJECTED (reject)
 *   COMPLETED   → (terminal)
 *   REJECTED    → (terminal)
 */
public enum RequestStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    REJECTED;

    public RequestStatus transitionAccept() {
        if (this != PENDING) {
            throw new IllegalStateException("error.request.accept_not_pending");
        }
        return IN_PROGRESS;
    }

    public RequestStatus transitionComplete() {
        if (this != IN_PROGRESS) {
            throw new IllegalStateException("error.request.complete_not_in_progress");
        }
        return COMPLETED;
    }

    public RequestStatus transitionReject() {
        if (this != PENDING && this != IN_PROGRESS) {
            throw new IllegalStateException("error.request.reject_invalid_state");
        }
        return REJECTED;
    }

    public boolean canBeEdited() {
        return this == PENDING;
    }

    public boolean canBeDeleted() {
        return this == PENDING;
    }
}
