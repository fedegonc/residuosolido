package com.residuosolido.app.service;

import com.residuosolido.app.event.RequestStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reacciona a RequestStatusChangedEvent fuera del hilo HTTP que aceptó o
 * rechazó la solicitud. No hay MongoTransactionManager configurado en este
 * proyecto (verificado: 0 beans PlatformTransactionManager, RequestService
 * no es un proxy transaccional) — @TransactionalEventListener(AFTER_COMMIT)
 * no serviría acá porque no existe un commit real del cual colgarse. Por
 * eso @Async simple: el evento se publica después del save exitoso (nunca
 * antes, RequestService lo garantiza), y esta clase corre en un hilo aparte
 * para que una escritura de notificación lenta no bloquee al usuario que
 * está aceptando/rechazando. Ver docs/TRADEOFFS.md §36.
 */
@Component
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async("notificationExecutor")
    @EventListener
    public void onRequestStatusChanged(RequestStatusChangedEvent event) {
        try {
            notificationService.notifyRequester(event.request(), event.type());
        } catch (Exception e) {
            // Falla de notificación NO debe propagarse: el estado de la solicitud
            // ya se guardó exitosamente antes de publicar este evento. Perder una
            // notificación es recuperable (el usuario ve el estado igual al entrar
            // a /mis-solicitudes); revertir una transición ya persistida no lo es.
            logger.error("NOTIFICATION_ASYNC_FAILED: requestId={}, type={}, error={}",
                    event.request().getId(), event.type(), e.getMessage(), e);
        }
    }
}
