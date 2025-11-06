package com.residuosolido.app.model.notification;

/**
 * Tipos de notificaciones disponibles en el sistema
 */
public enum NotificationType {
    INFO("info", "Información", "bg-blue-100 text-blue-800"),
    SUCCESS("success", "Éxito", "bg-green-100 text-green-800"),
    WARNING("warning", "Advertencia", "bg-yellow-100 text-yellow-800"),
    ERROR("error", "Error", "bg-red-100 text-red-800"),
    REQUEST_ASSIGNED("request_assigned", "Solicitud Asignada", "bg-purple-100 text-purple-800"),
    REQUEST_COMPLETED("request_completed", "Solicitud Completada", "bg-green-100 text-green-800"),
    REQUEST_CANCELLED("request_cancelled", "Solicitud Cancelada", "bg-red-100 text-red-800"),
    SYSTEM("system", "Sistema", "bg-gray-100 text-gray-800");

    private final String code;
    private final String displayName;
    private final String cssClass;

    NotificationType(String code, String displayName, String cssClass) {
        this.code = code;
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }
}
