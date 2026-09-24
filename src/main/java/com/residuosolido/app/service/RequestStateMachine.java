package com.residuosolido.app.service;

import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.StateException;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Request;
import org.springframework.stereotype.Component;

/**
 * Máquina de estados centralizada para Request.
 * Encapsula todas las transiciones válidas y su semántica.
 *
 * Contrato: métodos que modifican un Request lanzan excepción o devuelven silenciosamente.
 * Los métodos no comprueban permisos: eso es responsabilidad del controller/servicio.
 */
@Component
public class RequestStateMachine {

    /**
     * Transición PENDING → IN_PROGRESS.
     * Requiere: slot no nulo, status == PENDING.
     */
    public void accept(Request request, TimeSlot slot) {
        if (slot == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_SLOT_REQUIRED);
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new StateException(ServerMessage.ERROR_REQUEST_ACCEPT_NOT_PENDING);
        }
        request.accept(slot);
    }

    /**
     * Transición IN_PROGRESS → COMPLETED.
     * Requiere: status == IN_PROGRESS.
     */
    public void complete(Request request) {
        if (request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new StateException(ServerMessage.ERROR_REQUEST_COMPLETE_NOT_IN_PROGRESS);
        }
        request.complete();
    }

    /**
     * Transición PENDING|IN_PROGRESS → REJECTED.
     * Requiere: status == PENDING o IN_PROGRESS.
     */
    public void reject(Request request) {
        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.IN_PROGRESS) {
            throw new StateException(ServerMessage.ERROR_REQUEST_REJECT_INVALID_STATE);
        }
        request.reject();
    }

    /**
     * Comprueba si una solicitud puede transicionar a PENDING (p.ej. al editar).
     * Solo las solicitudes en PENDING pueden editarse.
     */
    public boolean canEdit(Request request) {
        return request.getStatus() == RequestStatus.PENDING;
    }

    /**
     * Comprueba si una solicitud puede eliminarse.
     * Solo las solicitudes en PENDING pueden eliminarse.
     */
    public boolean canDelete(Request request) {
        return request.getStatus() == RequestStatus.PENDING;
    }

    /**
     * Comprueba si una solicitud es aceptable.
     * Solo PENDING puede aceptarse.
     */
    public boolean canAccept(Request request) {
        return request.getStatus() == RequestStatus.PENDING;
    }

    /**
     * Comprueba si una solicitud puede completarse.
     * Solo IN_PROGRESS puede completarse.
     */
    public boolean canComplete(Request request) {
        return request.getStatus() == RequestStatus.IN_PROGRESS;
    }

    /**
     * Comprueba si una solicitud puede rechazarse.
     * PENDING e IN_PROGRESS pueden rechazarse.
     */
    public boolean canReject(Request request) {
        return request.getStatus() == RequestStatus.PENDING || request.getStatus() == RequestStatus.IN_PROGRESS;
    }
}
