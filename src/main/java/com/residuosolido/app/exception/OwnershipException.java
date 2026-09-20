package com.residuosolido.app.exception;

import com.residuosolido.app.enums.ServerMessage;

/**
 * Intento de operar sobre un recurso que no pertenece al actor (solicitud
 * de otro usuario u otra organización). Extiende SecurityException para
 * preservar el dispatch existente.
 */
public class OwnershipException extends SecurityException implements Keyed {

    private final ServerMessage key;

    public OwnershipException(ServerMessage key) {
        super(key.code());
        this.key = key;
    }

    @Override
    public ServerMessage key() {
        return key;
    }
}
