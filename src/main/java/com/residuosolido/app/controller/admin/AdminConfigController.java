package com.residuosolido.app.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/config")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {

    @GetMapping
    public String config(Model model) {
        // Configuraciones básicas del sistema
        Map<String, String> configurations = new HashMap<>();
        configurations.put("app.name", "Residuo Sólido");
        configurations.put("app.version", "1.0.0");
        configurations.put("app.environment", "production");
        configurations.put("mail.enabled", "true");
        configurations.put("uploads.max-size", "10MB");
        configurations.put("session.timeout", "30");
        
        model.addAttribute("configurations", configurations);
        model.addAttribute("viewType", "list");
        return "admin/config";
    }

    @PostMapping("/update")
    public String updateConfig(@RequestParam Map<String, String> params,
                              RedirectAttributes ra) {
        try {
            // Simular actualización de configuraciones
            // En una implementación real, aquí se guardarían en base de datos o archivos de configuración
            ra.addFlashAttribute("successMessage", "Configuraciones actualizadas correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al actualizar configuraciones: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }
}
