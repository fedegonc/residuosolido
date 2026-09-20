package com.residuosolido.app.exception;

import com.residuosolido.app.enums.ServerMessage;

/**
 * Violación de estado o invariante del dominio (transición ilegal, perfil
 * incompleto, modificación concurrente). Extiende IllegalStateException para
 * preservar el dispatch existente en controllers y handler global.
 */
public class StateException extends IllegalStateException implements Keyed {

    private final ServerMessage key;

    public StateException(ServerMessage key) {
        super(key.code());
        this.key = key;
    }

    public StateException(ServerMessage key, Throwable cause) {
        super(key.code(), cause);
        this.key = key;
    }

    @Override
    public ServerMessage key() {
        return key;
    }
}
