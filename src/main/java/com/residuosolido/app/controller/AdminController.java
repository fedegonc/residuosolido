package com.residuosolido.app.controller;

import com.residuosolido.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.residuosolido.app.service.StatisticsService;



@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private ConfigService configService;

    @Autowired
    private StatisticsService statisticsService;

    // ========== DASHBOARD & DOCS ==========
    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/documentation")
    public String documentation() {
        return "admin/documentation";
    }

    @GetMapping("/admin/documentation/uml")
    public String documentationUml() {
        return "admin/uml";
    }
    
    @GetMapping("/admin/documentation/layouts")
    public String documentationLayouts() {
        return "admin/documentation-layouts";
    }

    @GetMapping("/admin/documentation/details/{section}")
    public String showDocumentationDetails(@PathVariable String section, Model model) {
        model.addAttribute("section", section);
        return "admin/documentation-details";
    }

    @GetMapping("/admin/documentation/details/visual-system")
    public String showVisualSystem(Model model) {
        model.addAttribute("section", "visual-system");
        return "admin/documentation-details";
    }

    @GetMapping("/admin/statistics")
    public String statistics(Model model) {
        try {
            // Obtener estadísticas dinámicas
            var stats = statisticsService.getDashboardStats();
            model.addAttribute("stats", stats);

            // Obtener datos para gráficos
            var usersByMonth = statisticsService.getUsersByMonth();
            var requestsByStatus = statisticsService.getRequestsByStatus();
            var materialsByType = statisticsService.getMaterialsByType();

            model.addAttribute("usersByMonth", usersByMonth);
            model.addAttribute("requestsByStatus", requestsByStatus);
            model.addAttribute("materialsByType", materialsByType);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar estadísticas: " + e.getMessage());
        }

        return "admin/statistics";
    }

    // ========== CONFIG ==========
    @GetMapping("/admin/config")
    public String config() {
        return "admin/config";
    }

    @PostMapping("/admin/config/hero-image")
    public String updateHeroImage(@RequestParam("heroImageFile") MultipartFile heroImageFile, 
                                 RedirectAttributes redirectAttributes) {
        try {
            if (heroImageFile != null && !heroImageFile.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(heroImageFile);
                configService.setHeroImageUrl(imageUrl); // Guardar URL en configuración
                redirectAttributes.addFlashAttribute("successMessage", "Imagen hero actualizada correctamente");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No se seleccionó ninguna imagen");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar imagen: " + e.getMessage());
        }
        return "redirect:/admin/config";
    }
    
    // ========== PASSWORD RESET ==========
    @GetMapping("/admin/password-reset-requests")
    public String passwordResetRequests() {
        return "admin/password-reset-requests";
    }

    @PostMapping("/admin/password-reset-requests")
    public String handlePasswordReset(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        // TODO: Implement password reset logic
        return "redirect:/admin/password-reset-requests";
    }

}
