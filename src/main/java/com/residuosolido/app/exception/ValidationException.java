package com.residuosolido.app.exception;


/**
 * Error de validación de input (teléfono, campos requeridos, formato).
 * Extiende IllegalArgumentException para que los catch sites y el
 * @ExceptionHandler existentes la atrapen sin cambios.
 */
public class ValidationException extends IllegalArgumentException implements Keyed {

    private final ServerMessage key;

    public ValidationException(ServerMessage key) {
        super(key.code());
        this.key = key;
    }

    @Override
    public ServerMessage key() {
        return key;
    }
}
