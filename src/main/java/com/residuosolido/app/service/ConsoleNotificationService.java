package com.residuosolido.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementación de respaldo que loguea los mensajes de WhatsApp.
 * Útil para desarrollo y testing hasta que se configure Twilio/WhatsApp Business.
 * 
 * Configurar con: notifications.enabled=false para desactivar.
 */
@Service
public class ConsoleNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleNotificationService.class);

    @Value("${notifications.enabled:true}")
    private boolean enabled;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void sendWhatsApp(String toPhone, String message) {
        if (!enabled) {
            logger.debug("[DEV] Notificación deshabilitada — teléfono: {}", toPhone);
            return;
        }
        logger.info("[DEV] WhatsApp a {} | Mensaje: {}", toPhone, message);
    }
}
