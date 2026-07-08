package com.residuosolido.app.model;

/**
 * Categorías fijas de materiales reciclables.
 * Usar enum en lugar de String para type safety y validación.
 */
public enum MaterialCategory {
    PLASTICO,
    PAPEL_CARTON,
    VIDRIO,
    METAL,
    ORGANICO,
    ELECTRONICO,
    TEXTIL,
    OTROS
}
