package com.residuosolido.app.model;

/**
 * Enum that defines the possible user roles in the system with their specific dashboard URLs
 */
public enum Role {
    ADMIN("admin", "/admin/dashboard", 100),       // System administrator
    ORGANIZATION("org", "/acopio/inicio", 50),     // Organization representative  
    USER("user", "/usuarios/inicio", 10);          // Normal user who can request collections
    
    private final String urlPrefix;
    private final String dashboardUrl;
    private final int priority;
    
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
    
    /**
     * Returns display name for UI. Consider using MessageSource for i18n in the future.
     */
    public String getDisplayName() {
        switch (this) {
            case ADMIN:
                return "Administrador";
            case ORGANIZATION:
                return "Organización";
            case USER:
                return "Usuario";
        }
        // No default case - compiler will warn if new roles are added
        throw new IllegalStateException("Unexpected role: " + this);
    }
}
