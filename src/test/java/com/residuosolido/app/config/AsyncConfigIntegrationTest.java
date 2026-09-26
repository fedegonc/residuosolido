package com.residuosolido.app.config;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.event.RequestStatusChangedEvent;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.service.NotificationService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * Verifica que @Async en NotificationEventListener NO sea decorativo, con el
 * contexto real de Spring — la misma clase de bug que se encontró con
 * @Transactional (0 PlatformTransactionManager beans, la anotación no hacía
 * nada, ver MEJORAS.md #208). No hace falta un test tan caro para @Async
 * como para @Transactional: alcanza con capturar en qué hilo corre el
 * listener cuando se publica el evento por el mecanismo real de Spring.
 */
@Tag("integration")
@SpringBootTest(properties = {
        "spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/testdb}",
        "spring.data.mongodb.auto-index-creation=false",
        "app.seed=false"
})
class AsyncConfigIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MockBean
    private NotificationService notificationService;

    @Test
    void notificationEventListener_runsOnDifferentThreadThanPublisher() throws InterruptedException {
        String publisherThread = Thread.currentThread().getName();
        AtomicReference<String> listenerThread = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
            listenerThread.set(Thread.currentThread().getName());
            latch.countDown();
            return null;
        }).when(notificationService).notifyRequester(any(), any());

        Request request = Request.forCitizen(TestFixtures.citizen("u1", "+59899123456"));
        eventPublisher.publishEvent(new RequestStatusChangedEvent(request, NotificationType.ACCEPTED));

        assertTrue(latch.await(5, TimeUnit.SECONDS), "El listener nunca se ejecutó");
        assertNotEquals(publisherThread, listenerThread.get(),
                "El listener corrió en el mismo hilo que publicó el evento — @Async no se está aplicando, es decorativo como pasaba con @Transactional (ver MEJORAS.md #208)");
        assertTrue(listenerThread.get().startsWith("notif-"),
                "Se esperaba el pool nombrado de AsyncConfig ('notif-*'), corrió en: " + listenerThread.get());
    }
}
