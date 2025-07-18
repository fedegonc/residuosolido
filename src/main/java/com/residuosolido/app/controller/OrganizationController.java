package com.residuosolido.app.controller;

import com.residuosolido.app.repository.OrganizationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    public OrganizationController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @GetMapping("/organizaciones")
    public String listOrganizations(Model model) {
        model.addAttribute("organizations", organizationRepository.findAll());
        return "organizaciones";
    }
    
    @GetMapping("/org/dashboard")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public String organizationDashboard(Model model) {
        // Obtener estadísticas para el dashboard de organización
        long totalOrganizations = organizationRepository.count();
        
        // Agregar datos al modelo
        model.addAttribute("totalOrganizations", totalOrganizations);
        model.addAttribute("pageTitle", "Panel de Organización");
        
        return "org/dashboard";
    }
}
