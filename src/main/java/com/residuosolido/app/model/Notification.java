package com.residuosolido.app.model;

import com.residuosolido.app.enums.NotificationType;
import com.residuosolido.app.enums.TimeSlot;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.LocalDateTime;

/**
 * Bandeja in-app del ciudadano: una entrada por notificación enviada.
 * Solo usuarios registrados tienen bandeja — el invitado no tiene cuenta;
 * su canal (SMS/WhatsApp sobre guestPhone) es un adapter diferido, ver
 * docs/MEJORAS.md #187.
 *
 * requestId se guarda como String (no DocumentReference): la solicitud puede
 * borrarse y la notificación histórica debe sobrevivir.
 */
@Document(collection = "notifications")
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
