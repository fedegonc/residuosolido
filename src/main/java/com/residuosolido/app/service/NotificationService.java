package com.residuosolido.app.service;

import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.model.Notification;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Bandeja in-app del ciudadano: persiste una {@link Notification} por cada
 * transición de la organización que el usuario debe conocer (aceptada/rechazada).
 *
 * Los invitados NO pasan por acá: no tienen cuenta ni bandeja. Su canal
 * (SMS/WhatsApp sobre guestPhone) es un adapter diferido — el contrato de
 * dominio ya está modelado en el sandbox (NotificationPort, recipient =
 * guestPhone), ver docs/MEJORAS.md #187.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Registra la notificación para el solicitante de la request.
     * Debe llamarse DESPUÉS del save de la request: si la transición falla por
     * concurrencia, no se notifica un estado que no quedó persistido.
     */
    public void notifyRequester(Request request, NotificationType type) {
        if (request.getUser() == null) return; // invitado: sin bandeja, canal externo diferido
        notificationRepository.save(
                new Notification(request.getUser(), request.getId(), type, request.getConfirmedSlot()));
    }

    public List<Notification> listFor(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long unreadCount(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }

    /** Marca como leídas las notificaciones listadas (el usuario ya las vio). */
    public void markRead(List<Notification> notifications) {
        List<Notification> unread = notifications.stream().filter(n -> !n.isRead()).toList();
        if (!unread.isEmpty()) {
            unread.forEach(Notification::markRead);
            notificationRepository.saveAll(unread);
        }
    }
}
