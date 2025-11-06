package com.residuosolido.app.controller.notification;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.notification.Notification;
import com.residuosolido.app.service.UserService;
import com.residuosolido.app.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para gestionar las notificaciones
 */
@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    /**
     * Página principal de notificaciones
     */
    @GetMapping("/notificaciones")
    public String notificationPage(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication,
            Model model) {
        
        User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
        
        if (q != null && !q.isEmpty()) {
            Page<Notification> notifications = notificationService.searchByUser(
                    currentUser, 
                    q, 
                    PageRequest.of(page, size, Sort.by("createdAt").descending())
            );
            model.addAttribute("notifications", notifications);
            model.addAttribute("query", q);
            log.info("[Notifications/Page] user={} query='{}' page={} size={} returned={} total={}",
                    currentUser.getId() + ":" + currentUser.getUsername(), q, page, size,
                    notifications.getNumberOfElements(), notifications.getTotalElements());
            notifications.getContent().forEach(n ->
                    log.info("  - id={} read={} type={} title=\"{}\"",
                            n.getId(), n.getIsRead(), n.getType(), n.getTitle()));
        } else {
            List<Notification> notifications = notificationService.findAllByUser(currentUser);
            model.addAttribute("notifications", notifications);
            log.info("[Notifications/Page] user={} totalLoaded={} (no query)",
                    currentUser.getId() + ":" + currentUser.getUsername(), notifications.size());
            notifications.forEach(n ->
                    log.info("  - id={} read={} type={} title=\"{}\"",
                            n.getId(), n.getIsRead(), n.getType(), n.getTitle()));
        }
        
        model.addAttribute("unreadCount", notificationService.countUnreadByUser(currentUser));
        log.info("[Notifications/Page] user={} unreadCount={}",
                currentUser.getId() + ":" + currentUser.getUsername(),
                notificationService.countUnreadByUser(currentUser));
        return "notification/list";
    }

    /**
     * Marcar una notificación como leída
     */
    @PostMapping("/notificaciones/{id}/marcar-leida")
    public String markAsRead(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
        Optional<Notification> notification = notificationService.findById(id);
        
        if (notification.isPresent() && notification.get().getUser() != null 
                && notification.get().getUser().getId().equals(currentUser.getId())) {
            notificationService.markAsRead(id);
        }
        
        return "redirect:/notificaciones";
    }

    /**
     * Marcar todas las notificaciones como leídas
     */
    @PostMapping("/notificaciones/marcar-todas-leidas")
    public String markAllAsRead(Authentication authentication) {
        User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
        notificationService.markAllAsRead(currentUser);
        return "redirect:/notificaciones";
    }

    /**
     * Eliminar una notificación
     */
    @PostMapping("/notificaciones/{id}/eliminar")
    public String deleteNotification(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
        Optional<Notification> notification = notificationService.findById(id);
        
        if (notification.isPresent() && notification.get().getUser() != null 
                && notification.get().getUser().getId().equals(currentUser.getId())) {
            notificationService.deleteNotification(id);
        }
        
        return "redirect:/notificaciones";
    }

    /**
     * API para obtener el contador de notificaciones no leídas
     */
    @GetMapping("/api/notificaciones/contador")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
        long unreadCount = notificationService.countUnreadByUser(currentUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        
        log.info("[Notifications/API Count] user={} unreadCount={}",
                currentUser.getId() + ":" + currentUser.getUsername(), unreadCount);
        return ResponseEntity.ok(response);
    }

    /**
     * API para obtener las últimas notificaciones
     */
    @GetMapping("/api/notificaciones/ultimas")
    @ResponseBody
    public ResponseEntity<List<Notification>> getLatestNotifications(Authentication authentication) {
        User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
        List<Notification> notifications = notificationService.findLatestByUser(currentUser);
        log.info("[Notifications/API Latest] user={} returned={}",
                currentUser.getId() + ":" + currentUser.getUsername(), notifications.size());
        notifications.forEach(n ->
                log.info("  - id={} read={} type={} title=\"{}\"",
                        n.getId(), n.getIsRead(), n.getType(), n.getTitle()));
        return ResponseEntity.ok(notifications);
    }
}
