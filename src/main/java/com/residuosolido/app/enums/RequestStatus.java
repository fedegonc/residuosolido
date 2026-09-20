package com.residuosolido.app.enums;

import com.residuosolido.app.enums.ServerMessage;
import com.residuosolido.app.exception.StateException;

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
            throw new StateException(ServerMessage.ERROR_REQUEST_ACCEPT_NOT_PENDING);
        }
        return IN_PROGRESS;
    }

    public RequestStatus transitionComplete() {
        if (this != IN_PROGRESS) {
            throw new StateException(ServerMessage.ERROR_REQUEST_COMPLETE_NOT_IN_PROGRESS);
        }
        return COMPLETED;
    }

    public RequestStatus transitionReject() {
        if (this != PENDING && this != IN_PROGRESS) {
            throw new StateException(ServerMessage.ERROR_REQUEST_REJECT_INVALID_STATE);
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
