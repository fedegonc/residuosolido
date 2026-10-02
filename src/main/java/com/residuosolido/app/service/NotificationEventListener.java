package com.residuosolido.app.service;

import com.residuosolido.app.event.RequestStatusChangedEvent;
import com.residuosolido.app.model.NotificationEvent;
import com.residuosolido.app.repository.NotificationEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Reacciona a RequestStatusChangedEvent fuera del hilo HTTP.
 * Épica 14: Durable Event Queue — persiste eventos en MongoDB.
 * Si el procesamiento falla, el evento queda registrado para retry.
 */
@Component
public class NotificationEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;
    private final NotificationEventRepository eventRepository;

    public NotificationEventListener(NotificationService notificationService,
                                     NotificationEventRepository eventRepository) {
        this.notificationService = notificationService;
        this.eventRepository = eventRepository;
    }

    @Async("notificationExecutor")
    @EventListener
    public void onRequestStatusChanged(RequestStatusChangedEvent event) {
        String userId = event.request().getUser() != null ? event.request().getUser().getId() : null;
        String orgId = event.request().getOrganization() != null ? event.request().getOrganization().getId() : null;

        NotificationEvent notifEvent = new NotificationEvent(
                event.request().getId(),
                userId,
                orgId,
                event.type()
        );
        notifEvent = eventRepository.save(notifEvent);

        try {
            notificationService.notifyRequester(event.request(), event.type());
            notifEvent.markProcessed();
            eventRepository.save(notifEvent);
            logger.info("NOTIFICATION_SUCCESS: eventId={}, requestId={}", notifEvent.getId(), event.request().getId());
        } catch (RuntimeException e) {
            logger.error("NOTIFICATION_FAILED: eventId={}, requestId={}, type={}, error={}",
                    notifEvent.getId(), event.request().getId(), event.type(), e.getMessage(), e);
        }
    }
}
