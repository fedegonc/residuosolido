package com.residuosolido.app.service.notification;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.RequestStatus;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.notification.Notification;
import com.residuosolido.app.model.notification.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para enviar notificaciones automáticas del sistema
 * Este servicio actúa como una fachada para enviar notificaciones específicas
 * relacionadas con eventos del sistema
 */
@Service
public class SystemNotificationService {

    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(SystemNotificationService.class);

    @Autowired
    public SystemNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Notifica cuando una solicitud es asignada a una organización
     */ 
    public Notification notifyRequestAssigned(Request request) {
        // Notificar al usuario que creó la solicitud
        String title = "Solicitud asignada";
        String message = "Tu solicitud de recolección ha sido asignada a la organización " + 
                         request.getOrganization().getFullName() + ". Pronto se pondrán en contacto contigo.";
        String actionUrl = "/usuarios/solicitudes/" + request.getId();
        
        log.info("[SystemNotification/Assigned] requestId={} userId={} orgId={}",
                request.getId(),
                (request.getUser() != null ? request.getUser().getId() : null),
                (request.getOrganization() != null ? request.getOrganization().getId() : null));

        return notificationService.createNotification(
            request.getUser(),
            title,
            message,
            NotificationType.REQUEST_ASSIGNED,
            actionUrl
        );
    }

    /**
     * Notifica cuando una solicitud cambia de estado
     */
    public Notification notifyRequestStatusChange(Request request, RequestStatus oldStatus) {
        // Solo notificar cambios relevantes
        if (oldStatus == request.getStatus()) {
            return null;
        }

        String title;
        String message;
        NotificationType type;
        String actionUrl = "/usuarios/solicitudes/" + request.getId();

        switch (request.getStatus()) {
            case ACCEPTED:
                title = "Solicitud aceptada";
                message = "Tu solicitud de recolección fue aceptada por la organización " +
                         request.getOrganization().getFullName() + ". Pronto coordinarán la recolección.";
                type = NotificationType.INFO;
                break;
            case IN_PROGRESS:
                title = "Solicitud en proceso";
                message = "Tu solicitud de recolección está siendo procesada. La organización " +
                         request.getOrganization().getFullName() + " está trabajando en ella.";
                type = NotificationType.INFO;
                break;
            case COMPLETED:
                title = "Solicitud completada";
                message = "¡Tu solicitud de recolección ha sido completada con éxito! Gracias por contribuir al reciclaje.";
                type = NotificationType.REQUEST_COMPLETED;
                break;
            case REJECTED:
                title = "Solicitud rechazada";
                message = "Tu solicitud de recolección fue rechazada por la organización. Si tienes dudas, contáctalos para más información.";
                type = NotificationType.ERROR;
                break;
            default:
                return null; // No notificar otros estados
        }

        log.info("[SystemNotification/StatusChange] requestId={} oldStatus={} newStatus={} type={}",
                request.getId(), oldStatus, request.getStatus(), type);

        return notificationService.createNotification(
            request.getUser(),
            title,
            message,
            type,
            actionUrl
        );
    }

    /**
     * Notifica a la organización cuando recibe una nueva solicitud
     */
    public Notification notifyNewRequestToOrganization(Request request) {
        if (request.getOrganization() == null) {
            return null;
        }

        String title = "Nueva solicitud recibida";
        String message = "Has recibido una nueva solicitud de recolección de " + 
                         request.getUser().getFullName() + ". Revisa los detalles para procesarla.";
        String actionUrl = "/acopio/solicitudes/" + request.getId();

        log.info("[SystemNotification/NewRequestToOrg] requestId={} orgId={} userId={}",
                request.getId(),
                (request.getOrganization() != null ? request.getOrganization().getId() : null),
                (request.getUser() != null ? request.getUser().getId() : null));

        return notificationService.createNotification(
            request.getOrganization(),
            title,
            message,
            NotificationType.INFO,
            actionUrl
        );
    }

    /**
     * Envía una notificación de bienvenida a un nuevo usuario
     */
    public Notification sendWelcomeNotification(User user) {
        String title = "¡Bienvenido a ResiduoSolido!";
        String message = "Gracias por unirte a nuestra plataforma. Estamos felices de tenerte como parte " +
                         "de nuestra comunidad comprometida con el medio ambiente.";
        String actionUrl = "/usuarios/inicio";

        log.info("[SystemNotification/Welcome] userId={}", (user != null ? user.getId() : null));

        return notificationService.createNotification(
            user,
            title,
            message,
            NotificationType.SUCCESS,
            actionUrl
        );
    }

    /**
     * Envía una notificación personalizada a un usuario
     */
    public Notification sendCustomNotification(User user, String title, String message, NotificationType type, String actionUrl) {
        log.info("[SystemNotification/Custom] userId={} type={} title=\"{}\" actionUrl={}",
                (user != null ? user.getId() : null), type, title, actionUrl);
        return notificationService.createNotification(
            user,
            title,
            message,
            type,
            actionUrl
        );
    }
}
