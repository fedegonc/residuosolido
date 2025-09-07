package com.residuosolido.app.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {

    @GetMapping("/admin/config")
    public String config(Model model) {
        // Configuraciones del sistema
        model.addAttribute("appName", "Sistema de Gestión de Residuos Sólidos");
        model.addAttribute("version", "1.0.0");
        model.addAttribute("environment", "production");
        model.addAttribute("maxFileSize", "10MB");
        model.addAttribute("emailEnabled", true);
        model.addAttribute("maintenanceMode", false);
        
        return "admin/config";
    }

    @PostMapping("/admin/config/maintenance")
    public String toggleMaintenance(@RequestParam boolean enabled, 
                                   RedirectAttributes ra) {
        try {
            // Aquí iría la lógica para activar/desactivar modo mantenimiento
            String message = enabled ? 
                "Modo mantenimiento activado" : 
                "Modo mantenimiento desactivado";
            ra.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }

    @PostMapping("/admin/config/cache")
    public String clearCache(RedirectAttributes ra) {
        try {
            // Aquí iría la lógica para limpiar caché
            ra.addFlashAttribute("successMessage", "Caché limpiado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al limpiar caché: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }
}
