package com.residuosolido.app.repository.notification;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.notification.Notification;
import com.residuosolido.app.model.notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Encuentra todas las notificaciones para un usuario específico
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Encuentra las notificaciones no leídas para un usuario específico
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /**
     * Cuenta las notificaciones no leídas para un usuario específico
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * Encuentra las notificaciones por tipo para un usuario específico
     */
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);

    /**
     * Encuentra las notificaciones creadas después de una fecha específica
     */
    List<Notification> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date);

    /**
     * Busca notificaciones por contenido
     */
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(n.message) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Notification> searchByUserAndQuery(@Param("user") User user, @Param("query") String query, Pageable pageable);

    /**
     * Encuentra las últimas N notificaciones para un usuario
     */
    List<Notification> findTop5ByUserOrderByCreatedAtDesc(User user);
}
