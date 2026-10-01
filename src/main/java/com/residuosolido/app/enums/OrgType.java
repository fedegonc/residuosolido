package com.residuosolido.app.enums;

/**
 * Tipo de organización — vocabulario cerrado que define el rol operativo
 * dentro de "organización". Punto de extensión para permisos y UI por
 * variante (un recolector informal no es un centro fijo).
 */
public enum OrgType {
    /** Punto fijo que recibe materiales de ciudadanos. */
    CENTRO_ACOPIO,
    /** Clasifica y compacta; vende bultos, no recicla por sí misma. */
    SELECCION_Y_PRENSADO,
    /** Organización de recicladores asociados. */
    COOPERATIVA,
    /** Recolección móvil individual. */
    RECOLECTOR_INFORMAL
}
