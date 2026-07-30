package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.service.BreadcrumbService;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('USER')")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    private final UserService userService;
    private final RequestService requestService;
    private final RequestMetricsService requestMetricsService;
    private final MessageSource messageSource;
    private final BreadcrumbService breadcrumbService;

    @Autowired
    public UserProfileController(UserService userService, RequestService requestService,
                                 RequestMetricsService requestMetricsService, MessageSource messageSource,
                                 BreadcrumbService breadcrumbService) {
        this.userService = userService;
        this.requestService = requestService;
        this.requestMetricsService = requestMetricsService;
        this.messageSource = messageSource;
        this.breadcrumbService = breadcrumbService;
    }

    @GetMapping("/usuarios/inicio")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.findAuthenticatedUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("recentRequests", requestService.getRecentRequestsByUser(user, 5));
        model.addAttribute("breadcrumbs", breadcrumbService.addCurrent(breadcrumbService.home(), "Mi panel"));
        return "users/dashboard";
    }

    @GetMapping("/usuarios/perfil")
    public String profile(Authentication authentication, Model model) {
        User user = userService.findAuthenticatedUserByUsername(authentication.getName());
        model.addAttribute("user", user);
        model.addAttribute("userForm", user);
        model.addAttribute("requestStats", requestMetricsService.getUserDashboardStats(user));
        model.addAttribute("breadcrumbs", breadcrumbService.addCurrent(breadcrumbService.add(breadcrumbService.home(), "Mi panel", "/usuarios/inicio"), "Mi perfil"));
        return "users/profile";
    }

    @PostMapping("/usuarios/perfil")
    public String updateProfile(@RequestParam(required = false) String email,
                                @RequestParam(required = false) String firstName,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) City city,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findAuthenticatedUserByUsername(authentication.getName());
            userService.updateProfile(user, email, firstName, phone, city);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.profile.updated", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            logger.error("Error al actualizar perfil de usuario: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.profile.update_error", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/usuarios/perfil";
    }
}
