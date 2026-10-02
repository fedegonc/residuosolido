package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import com.residuosolido.app.model.Notification;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bandeja in-app del ciudadano: lista paginada de notificaciones
 * (aceptada/rechazada por la organización). Ver la página marca todo como leído
 * — las que estaban sin leer se muestran con el sello "nueva" en esta carga.
 */
@Controller
@PreAuthorize("hasRole('USER')")
public class NotificationController {

    private static final int PAGE_SIZE = 10;
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping(Routes.NOTIFICATIONS)
    public String notifications(@CurrentUser User user,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {
        Page<Notification> notificationsPage = notificationService.listForPaged(
                user,
                PageRequest.of(page, PAGE_SIZE)
        );
        Set<String> freshIds = notificationsPage.getContent().stream()
                .filter(n -> !n.isRead())
                .map(Notification::getId)
                .collect(Collectors.toSet());
        notificationService.markRead(notificationsPage.getContent());
        model.addAttribute("notifications", notificationsPage);
        model.addAttribute("freshIds", freshIds);
        return "users/notifications";
    }
}
