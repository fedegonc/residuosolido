package com.residuosolido.app.enums;

/**
 * Tipo de notificación al solicitante. Solo las transiciones que el ciudadano
 * no puede observar por sí mismo (aceptación/rechazo de la organización)
 * generan notificación; completar y editar no notifican.
 */
public enum NotificationType {
    ACCEPTED,
    REJECTED
}
