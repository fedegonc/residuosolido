package com.residuosolido.app.model;

import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.enums.TimeSlot;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

/**
 * Bandeja in-app del ciudadano: una entrada por notificación enviada.
 * Toda solicitud pertenece a un usuario registrado — el destinatario es
 * siempre un userId con bandeja.
 *
 * requestId se guarda como String (no DocumentReference): la solicitud puede
 * borrarse y la notificación histórica debe sobrevivir.
 */
/* Índice compuesto (user, createdAt desc): cubre findByUserOrderByCreatedAtDesc
   (bandeja) y countByUserAndReadFalse (badge) sin collection scan. La creación
   real la hace MongoIndexInitializer — auto-index-creation está deshabilitado. */
@Document(collection = "notifications")
@CompoundIndex(name = "user_createdAt", def = "{'user': 1, 'createdAt': -1}")
public class Notification {

    @Id
    private String id;

    @DocumentReference(lazy = true)
    private User user;

    private String requestId;
    private NotificationType type;
    private TimeSlot confirmedSlot; // solo ACCEPTED: franja que confirmó la org
    private boolean read = false;
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(User user, String requestId, NotificationType type, TimeSlot confirmedSlot) {
        this.user = user;
        this.requestId = requestId;
        this.type = type;
        this.confirmedSlot = confirmedSlot;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }

    public String getRequestId() { return requestId; }

    public NotificationType getType() { return type; }

    public TimeSlot getConfirmedSlot() { return confirmedSlot; }

    public boolean isRead() { return read; }
    public void markRead() { this.read = true; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
