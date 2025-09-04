package com.residuosolido.app.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DashboardController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/dashboard")
    public String dashboard() {
        // Retorna el template unificado de dashboard
        return "dashboard";
    }

    // Compatibilidad con rutas antiguas de USER/ORG: redirigen al dashboard unificado
    @PreAuthorize("isAuthenticated()")
    @GetMapping({"/users/dashboard", "/org/dashboard"})
    public String legacyUserOrgDashboardsRedirect() {
        return "redirect:/dashboard";
    }
}
