package com.residuosolido.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementación de respaldo que loguea los mensajes de WhatsApp.
 * Útil para desarrollo y testing hasta que se configure Twilio/WhatsApp Business.
 */
@Service
public class ConsoleNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleNotificationService.class);

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void sendWhatsApp(String toPhone, String message) {
        logger.info("[WhatsApp a {}] {}", toPhone, message);
    }
}
