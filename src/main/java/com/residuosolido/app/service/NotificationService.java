package com.residuosolido.app.service;

/**
 * Abstracción para enviar notificaciones al usuario.
 * Implementación concreta inyectable según el proveedor configurado.
 */
public interface NotificationService {

    boolean isEnabled();

    void sendWhatsApp(String toPhone, String message);
}
