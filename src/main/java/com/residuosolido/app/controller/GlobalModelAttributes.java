package com.residuosolido.app.controller;

import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.NotificationService;
import com.residuosolido.app.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Atributos de modelo globales: hoy solo {@code unreadNotifications}, el contador
 * del badge de la campana en el navbar. Bean legítimo (@ControllerAdvice es la
 * interfaz de Spring para esto) — resuelve el usuario igual que
 * {@link CurrentUserArgumentResolver}: SecurityContext + findAuthenticatedUserByUsername.
 *
 * Costo conocido: una query find + una count por página para usuarios logueados.
 * Aceptable a esta escala; si crece, cachear en sesión o mover a un fragment
 * cargado bajo demanda.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;
    private final NotificationService notificationService;

    public GlobalModelAttributes(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadNotifications")
    public Long unreadNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (userService.isAnonymous(auth)) return null;
        User user;
        try {
            user = userService.findAuthenticatedUserByUsername(auth.getName());
        } catch (RuntimeException e) {
            return null; // sesión válida pero usuario inexistente: sin badge, nunca 500 global
        }
        if (user == null || user.getRole() != Role.USER) return null;
        return notificationService.unreadCount(user);
    }
}
