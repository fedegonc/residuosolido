package com.residuosolido.app.model;

import com.residuosolido.app.enums.NotificationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notification_events")
public class NotificationEvent {

    @Id
    private String id;

    private String requestId;
    private String userId;
    private String organizationId;

    private NotificationType type;
    private LocalDateTime createdAt;
    private boolean processed = false;
    private LocalDateTime processedAt;

    public NotificationEvent() {}

    public NotificationEvent(String requestId, String userId, String organizationId, NotificationType type) {
        this.requestId = requestId;
        this.userId = userId;
        this.organizationId = organizationId;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public void markProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }
}
