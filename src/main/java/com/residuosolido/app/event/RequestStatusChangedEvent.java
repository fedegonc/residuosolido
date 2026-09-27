package com.residuosolido.app.event;

import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.model.Request;

/**
 * Se publica después de guardar con éxito una transición de estado
 * (aceptar/rechazar) — nunca antes. RequestService ya no conoce
 * NotificationService: publica esto y sigue, sin esperar a que la
 * notificación se escriba. Punto de extensión declarado en
 * docs/TRADEOFFS.md §33 para futuros canales (email/SMS): agregar otro
 * @EventListener, no tocar RequestService.
 */
public record RequestStatusChangedEvent(Request request, NotificationType type) {
}
