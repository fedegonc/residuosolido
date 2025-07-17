package com.residuosolido.app.controller;

import com.residuosolido.app.repository.OrganizationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/organizaciones")
public class OrganizationController {

    private final OrganizationRepository organizationRepository;

    public OrganizationController(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @GetMapping
    public String listOrganizations(Model model) {
        model.addAttribute("organizations", organizationRepository.findAll());
        return "organizaciones";
    }
}
