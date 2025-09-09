package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private FeedbackService feedbackService;

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
    public String statistics() {
        return "admin/statistics";
    }

    // ========== CONFIG ==========
    @GetMapping("/admin/config")
    public String config() {
        return "admin/config";
    }

    @PostMapping("/admin/config/hero-image")
    public String updateHeroImage(@RequestParam("heroImage") String heroImageUrl, 
                                 RedirectAttributes redirectAttributes) {
        try {
            // Lógica para actualizar imagen hero
            redirectAttributes.addFlashAttribute("successMessage", "Imagen hero actualizada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar imagen");
        }
        return "redirect:/admin/config";
    }
}
