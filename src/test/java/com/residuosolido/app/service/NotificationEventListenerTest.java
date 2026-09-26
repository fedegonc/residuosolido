package com.residuosolido.app.service;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.event.RequestStatusChangedEvent;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationEventListener es la mitad "consumidora" del desacople de
 * MEJORAS.md #208: RequestService publica el evento sin saber quién lo
 * escucha, así que el contrato correcto (recibir el evento → notificar con
 * los mismos datos) solo lo cubre un test dedicado a esta clase.
 */
@Tag("unit")
class NotificationEventListenerTest {

    private NotificationService notificationService;
    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        listener = new NotificationEventListener(notificationService);
    }

    private User citizen() {
        return TestFixtures.citizen("u1", "+59899123456");
    }

    @Test
    void onRequestStatusChanged_forwardsToNotificationServiceWithSameData() {
        Request request = Request.forCitizen(citizen());
        RequestStatusChangedEvent event = new RequestStatusChangedEvent(request, NotificationType.ACCEPTED);

        listener.onRequestStatusChanged(event);

        verify(notificationService).notifyRequester(request, NotificationType.ACCEPTED);
    }

    @Test
    void onRequestStatusChanged_notificationServiceThrows_doesNotPropagate() {
        Request request = Request.forCitizen(citizen());
        RequestStatusChangedEvent event = new RequestStatusChangedEvent(request, NotificationType.REJECTED);
        doThrow(new RuntimeException("mongo caído")).when(notificationService)
                .notifyRequester(any(Request.class), any(NotificationType.class));

        // No debe lanzar: el estado de la solicitud ya se persistió antes de
        // publicar el evento. Perder una notificación es recuperable; que
        // una excepción se escape de un @Async no tiene quién la atrape.
        listener.onRequestStatusChanged(event);

        verify(notificationService).notifyRequester(request, NotificationType.REJECTED);
    }
}
