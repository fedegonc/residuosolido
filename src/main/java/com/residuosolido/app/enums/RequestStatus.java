package com.residuosolido.app.enums;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.StateException;

/**
 * Máquina de estados finita de una solicitud.
 *
 * FSM invariantes:
 * - COMPLETED y REJECTED son estados terminales (no transiciones salientes)
 * - PENDING: entrada, solo hacia IN_PROGRESS o REJECTED
 * - IN_PROGRESS: solo hacia COMPLETED o REJECTED
 *
 * Predicados verificables: isTerminal(), canTransitionTo()
 */
public enum RequestStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    REJECTED;

    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED;
    }

    public boolean canTransitionTo(RequestStatus target) {
        return switch (this) {
            case PENDING -> target == IN_PROGRESS || target == REJECTED;
            case IN_PROGRESS -> target == COMPLETED || target == REJECTED;
            case COMPLETED, REJECTED -> false;
        };
    }

    public RequestStatus transitionAccept() {
        if (!canTransitionTo(IN_PROGRESS)) {
            throw new StateException(ServerMessage.ERROR_REQUEST_ACCEPT_NOT_PENDING);
        }
        return IN_PROGRESS;
    }

    public RequestStatus transitionComplete() {
        if (!canTransitionTo(COMPLETED)) {
            throw new StateException(ServerMessage.ERROR_REQUEST_COMPLETE_NOT_IN_PROGRESS);
        }
        return COMPLETED;
    }

    public RequestStatus transitionReject() {
        if (!canTransitionTo(REJECTED)) {
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
