package com.residuosolido.app.model;

/**
 * Enum that defines the possible user roles in the system
 */
public enum Role {
    ADMIN("admin", "/dashboard", 100),       // System administrator
    ORGANIZATION("org", "/dashboard", 50), // Organization representative
    USER("user", "/dashboard", 10);        // Normal user who can request collections
    
    private final String urlPrefix;
    private final String dashboardUrl;
    private final int priority;
    
    // Constructor para el enum Role
    Role(String urlPrefix, String dashboardUrl, int priority) {
        this.urlPrefix = urlPrefix;
        this.dashboardUrl = dashboardUrl;
        this.priority = priority;
    }
    
    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    public String getDashboardUrl() {
        return dashboardUrl;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public String getDisplayName() {
        switch (this) {
            case ADMIN:
                return "Administrador";
            case ORGANIZATION:
                return "Organización";
            case USER:
                return "Usuario";
            default:
                return this.name();
        }
    }
}
