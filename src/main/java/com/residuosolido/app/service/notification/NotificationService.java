package com.residuosolido.app.service.notification;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.notification.Notification;
import com.residuosolido.app.model.notification.NotificationType;
import com.residuosolido.app.repository.notification.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar las notificaciones del sistema
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Crea una nueva notificación para un usuario
     */
    @Transactional
    public Notification createNotification(User user, String title, String message, NotificationType type, String actionUrl) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setActionUrl(actionUrl);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        Notification saved = notificationRepository.save(notification);
        String userLabel = (user == null) ? "SYSTEM" : (user.getId() + ":" + user.getUsername());
        log.info("[Notification/Create] user={} type={} title=\"{}\" actionUrl={} id={}", userLabel, type, title, actionUrl, saved.getId());
        return saved;
    }

    /**
     * Crea una notificación de sistema (sin usuario específico)
     */
    @Transactional
    public Notification createSystemNotification(String title, String message, String actionUrl) {
        return createNotification(null, title, message, NotificationType.SYSTEM, actionUrl);
    }

    /**
     * Encuentra todas las notificaciones para un usuario
     */
    public List<Notification> findAllByUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Encuentra las notificaciones no leídas para un usuario
     */
    public List<Notification> findUnreadByUser(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Cuenta las notificaciones no leídas para un usuario
     */
    public long countUnreadByUser(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Marca una notificación como leída
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        notificationOpt.ifPresent(notification -> {
            notification.markAsRead();
            notificationRepository.save(notification);
            String userLabel = (notification.getUser() == null) ? "SYSTEM" : (notification.getUser().getId() + ":" + notification.getUser().getUsername());
            log.info("[Notification/MarkRead] id={} user={}", notification.getId(), userLabel);
        });
    }

    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        notificationRepository.saveAll(unreadNotifications);
        log.info("[Notification/MarkAllRead] user={} count={}", (user == null ? "SYSTEM" : user.getId() + ":" + user.getUsername()), unreadNotifications.size());
    }

    /**
     * Elimina una notificación
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        log.info("[Notification/Delete] id={}", notificationId);
    }

    /**
     * Busca notificaciones por contenido
     */
    public Page<Notification> searchByUser(User user, String query, Pageable pageable) {
        return notificationRepository.searchByUserAndQuery(user, query, pageable);
    }

    /**
     * Encuentra las últimas 5 notificaciones para un usuario
     */
    public List<Notification> findLatestByUser(User user) {
        return notificationRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Encuentra una notificación por ID
     */
    @Transactional
    public Optional<Notification> findById(Long id) {
        return notificationRepository.findById(id);
    }
}
