package com.residuosolido.app.service;

import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
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
 * El destinatario es siempre el userId del solicitante (toda solicitud
 * pertenece a un usuario registrado).
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
        if (request == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        if (request.getUser() == null) return; // defensivo: toda request tiene user, pero no explotar si un doc viejo no lo tiene
        notificationRepository.save(
                new Notification(request.getUser(), request.getId(), type, request.getConfirmedSlot()));
    }

    public List<Notification> listFor(User user) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long unreadCount(User user) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        return notificationRepository.countByUserAndReadFalse(user);
    }

    /** Marca como leídas las notificaciones listadas (el usuario ya las vio). */
    public void markRead(List<Notification> notifications) {
        if (notifications == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        List<Notification> unread = notifications.stream().filter(n -> !n.isRead()).toList();
        if (!unread.isEmpty()) {
            unread.forEach(Notification::markRead);
            notificationRepository.saveAll(unread);
        }
    }
}
