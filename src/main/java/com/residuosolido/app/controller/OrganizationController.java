package com.residuosolido.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.annotation.PostConstruct;

@Controller
@RequestMapping("/org")
public class OrganizationController {
    private static final Logger log = LoggerFactory.getLogger(OrganizationController.class);

    @PostConstruct
    public void init() {
        log.info("[ORG] OrganizationController initialized and registered");
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/dashboard")
    public String orgDashboard(Model model, Authentication auth) {
        // Datos mínimos para la vista (podemos ampliar luego)
        String username = (auth != null) ? auth.getName() : "anonymous";
        log.info("[ORG] GET /org/dashboard requested by {}", username);
        model.addAttribute("totalOrganizations", 0);
        return "org/dashboard";
    }

    // Endpoint de diagnóstico para verificar mapeo de /org/**
    @GetMapping("/ping")
    @ResponseBody
    public String ping(Authentication auth) {
        String username = (auth != null) ? auth.getName() : "anonymous";
        log.info("[ORG] GET /org/ping by {}", username);
        return "ok-org";
    }
}
