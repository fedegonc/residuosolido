package com.residuosolido.app.controller;

import com.residuosolido.app.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class InitController {

    private final DashboardService dashboardService;

    @Autowired
    public InitController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    
    @GetMapping("/init")
    public String init(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/invitados";
        
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN" -> "redirect:/admin/dashboard";
            case "ROLE_ORGANIZATION" -> "redirect:/org/dashboard";
            default -> "redirect:/users/dashboard";
        };
    }
    
    @GetMapping("/invitados")
    public String invitados(Model model) {
        java.util.Map<String, Object> pageData = dashboardService.getPublicPageData();
        model.addAllAttributes(pageData);
        return "index";
    }
}
