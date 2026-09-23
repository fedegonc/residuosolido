package com.residuosolido.app.exception;


/**
 * Contrato tipado para excepciones cuyo mensaje es una clave i18n server-side.
 * El tejido (controllers, handler global) extrae la clave sin depender de
 * parsear strings: `e instanceof Keyed k → k.key()`.
 */
public interface Keyed {

    ServerMessage key();
}
