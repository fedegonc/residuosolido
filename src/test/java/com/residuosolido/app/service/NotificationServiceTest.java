package com.residuosolido.app.service;

import com.residuosolido.app.TestFixtures;
import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.Notification;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@Tag("unit")
class NotificationServiceTest {

    private NotificationRepository notificationRepository;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        notificationService = new NotificationService(notificationRepository);
    }

    // ===== notifyRequester: solo usuarios registrados tienen bandeja (ver MEJORAS.md #187) =====

    @Test
    void notifyRequester_guestRequest_doesNotPersist() {
        Request request = Request.forGuest("Ana", "+59899123456", "codigo123");

        notificationService.notifyRequester(request, NotificationType.ACCEPTED);

        verifyNoInteractions(notificationRepository);
    }

    @Test
    void notifyRequester_registeredUser_persistsNotificationWithRequestSnapshot() {
        User citizen = TestFixtures.citizen("u1", "+59899123456");
        Request request = Request.forCitizen(citizen);
        request.setId("req-1");
        request.setConfirmedSlot(TimeSlot.MANANA);

        notificationService.notifyRequester(request, NotificationType.ACCEPTED);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertSame(citizen, saved.getUser());
        assertEquals("req-1", saved.getRequestId());
        assertEquals(NotificationType.ACCEPTED, saved.getType());
        assertEquals(TimeSlot.MANANA, saved.getConfirmedSlot());
        assertFalse(saved.isRead());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void notifyRequester_rejected_persistsRejectedType() {
        User citizen = TestFixtures.citizen("u1", "+59899123456");
        Request request = Request.forCitizen(citizen);
        request.setId("req-1");

        notificationService.notifyRequester(request, NotificationType.REJECTED);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(NotificationType.REJECTED, captor.getValue().getType());
        assertNull(captor.getValue().getConfirmedSlot());
    }

    // ===== listFor / unreadCount: delegación pura =====

    @Test
    void listFor_delegatesToRepositoryOrderedByCreatedAtDesc() {
        User citizen = TestFixtures.citizen("u1", "+59899123456");
        List<Notification> expected = List.of(new Notification(citizen, "r1", NotificationType.ACCEPTED, null));
        when(notificationRepository.findByUserOrderByCreatedAtDesc(citizen)).thenReturn(expected);

        assertSame(expected, notificationService.listFor(citizen));
    }

    @Test
    void unreadCount_delegatesToRepository() {
        User citizen = TestFixtures.citizen("u1", "+59899123456");
        when(notificationRepository.countByUserAndReadFalse(citizen)).thenReturn(3L);

        assertEquals(3L, notificationService.unreadCount(citizen));
    }

    // ===== markRead: solo persiste las que estaban sin leer =====

    @Test
    void markRead_mixedList_savesOnlyUnreadAndMarksThem() {
        User citizen = TestFixtures.citizen("u1", "+59899123456");
        Notification unread = new Notification(citizen, "r1", NotificationType.ACCEPTED, null);
        Notification alreadyRead = new Notification(citizen, "r2", NotificationType.REJECTED, null);
        alreadyRead.markRead();

        notificationService.markRead(List.of(unread, alreadyRead));

        assertTrue(unread.isRead());
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertEquals(List.of(unread), captor.getValue());
    }

    @Test
    void markRead_allAlreadyRead_doesNotPersist() {
        User citizen = TestFixtures.citizen("u1", "+59899123456");
        Notification read = new Notification(citizen, "r1", NotificationType.ACCEPTED, null);
        read.markRead();

        notificationService.markRead(List.of(read));

        verify(notificationRepository, never()).saveAll(anyList());
    }

    @Test
    void markRead_emptyList_doesNotPersist() {
        notificationService.markRead(List.of());
        verify(notificationRepository, never()).saveAll(anyList());
    }
}
