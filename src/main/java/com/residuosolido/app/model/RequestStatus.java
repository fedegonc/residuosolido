package com.residuosolido.app.model;

/**
 * Status enum for waste collection requests
 */
public enum RequestStatus {
    PENDING("Pendiente"),
    ACCEPTED("Aceptada"), 
    REJECTED("Rechazada"),
    COMPLETED("Completada");
    
    private final String displayName;
    
    RequestStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
