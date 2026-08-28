package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Dashboard y perfil del ciudadano: estadísticas, datos personales y edición. */
@Controller
@PreAuthorize("hasRole('USER')")
public class UserProfileController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    private final RequestQueryService requestQueryService;
    private final RequestMetricsService requestMetricsService;

    @Autowired
    public UserProfileController(RequestQueryService requestQueryService,
                                 RequestMetricsService requestMetricsService) {
        this.requestQueryService = requestQueryService;
        this.requestMetricsService = requestMetricsService;
    }

    /** Dashboard del ciudadano con estadísticas y solicitudes recientes. */
    @GetMapping("/usuarios/inicio")
    public String dashboard(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("recentRequests", requestQueryService.getRecentRequestsByUser(user, 5));
        model.addAttribute("requestStats", requestMetricsService.getUserDashboardStats(user));
        model.addAttribute("breadcrumbs", List.of(
                Map.of("label", "Inicio", "href", "/"),
                Map.of("label", "Mi panel", "href", "")
        ));
        return "users/dashboard";
    }

    /** Muestra el perfil del ciudadano con sus datos. */
    @GetMapping("/usuarios/perfil")
    public String profile(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("requestStats", requestMetricsService.getUserDashboardStats(user));
        model.addAttribute("cities", City.values());
        model.addAttribute("breadcrumbs", List.of(
                Map.of("label", "Inicio", "href", "/"),
                Map.of("label", "Mi panel", "href", "/usuarios/inicio"),
                Map.of("label", "Mi perfil", "href", "")
        ));
        return "users/profile";
    }

    /** Actualiza los datos del perfil del ciudadano. */
    @PostMapping("/usuarios/perfil")
    public String updateProfile(@RequestParam(required = false) String email,
                                @RequestParam(required = false) String firstName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) City city,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            userService.updateProfile(user, email, firstName, phone, city);
            flashSuccess(redirectAttributes, "flash.profile.updated");
        } catch (Exception e) {
            logger.error("Error al actualizar perfil de usuario: {}", e.getMessage(), e);
            flashError(redirectAttributes, "flash.profile.update_error");
        }
        return "redirect:/usuarios/perfil";
    }
}
