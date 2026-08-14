package com.residuosolido.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio de notificaciones por WhatsApp.
 * Implementación con logs de consola para desarrollo.
 * Configurar con: notifications.enabled=false para desactivar.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Value("${notifications.enabled:true}")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void sendWhatsApp(String toPhone, String message) {
        if (!enabled) {
            logger.debug("[DEV] Notificación deshabilitada — teléfono: {}", toPhone);
            return;
        }
        logger.info("[DEV] WhatsApp a {} | Mensaje: {}", toPhone, message);
    }
}
