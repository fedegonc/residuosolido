package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import com.residuosolido.app.model.Notification;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bandeja in-app del ciudadano: lista las notificaciones de sus solicitudes
 * (aceptada/rechazada por la organización). Ver la página marca todo como leído
 * — las que estaban sin leer se muestran con el sello "nueva" en esta carga.
 */
@Controller
@PreAuthorize("hasRole('USER')")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(Routes.NOTIFICATIONS)
    public String notifications(@CurrentUser User user, Model model) {
        List<Notification> notifications = notificationService.listFor(user);
        Set<String> freshIds = notifications.stream()
                .filter(n -> !n.isRead())
                .map(Notification::getId)
                .collect(Collectors.toSet());
        notificationService.markRead(notifications);
        model.addAttribute("notifications", notifications);
        model.addAttribute("freshIds", freshIds);
        return "users/notifications";
    }
}
