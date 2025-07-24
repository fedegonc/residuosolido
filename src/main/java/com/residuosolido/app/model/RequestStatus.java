package com.residuosolido.app.model;

/**
 * Enum que define los posibles estados de una solicitud de recolección
 */
public enum RequestStatus {
    
    PENDING("Pendiente", "La solicitud ha sido enviada y está esperando revisión"),
    REVIEWED("Revisada", "La solicitud ha sido revisada por la organización"),
    APPROVED("Aprobada", "La solicitud ha sido aprobada y está programada para recolección"),
    IN_PROGRESS("En Progreso", "La recolección está siendo realizada"),
    COMPLETED("Completada", "La recolección ha sido completada exitosamente"),
    CANCELLED("Cancelada", "La solicitud ha sido cancelada"),
    REJECTED("Rechazada", "La solicitud ha sido rechazada por la organización");

    private final String displayName;
    private final String description;

    RequestStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
