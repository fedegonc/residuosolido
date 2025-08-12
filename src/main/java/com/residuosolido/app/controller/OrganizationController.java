package com.residuosolido.app.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
 

@Controller
@RequestMapping("/org")
public class OrganizationController {

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/dashboard")
    public String orgDashboard(Model model) {
        // Datos mínimos para la vista (podemos ampliar luego)
        model.addAttribute("totalOrganizations", 0);
        return "org/dashboard";
    }
}
