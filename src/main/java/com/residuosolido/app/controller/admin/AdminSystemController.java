package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSystemController {

    private final FeedbackService feedbackService;
    private final RequestService requestService;
    private final ConfigService configService;
    private final CloudinaryService cloudinaryService;

    public AdminSystemController(FeedbackService feedbackService, RequestService requestService,
                               ConfigService configService, CloudinaryService cloudinaryService) {
        this.feedbackService = feedbackService;
        this.requestService = requestService;
        this.configService = configService;
        this.cloudinaryService = cloudinaryService;
    }

    // FEEDBACK MANAGEMENT
    @GetMapping("/feedback")
    public String listFeedback(Model model) {
        model.addAttribute("feedbacks", feedbackService.findAll());
        return "admin/feedback";
    }

    @PostMapping("/feedback/{id}/delete")
    public String deleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            feedbackService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Feedback eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar feedback: " + e.getMessage());
        }
        return "redirect:/admin/feedback";
    }

    // REQUESTS MANAGEMENT
    @GetMapping("/requests")
    public String listRequests(Model model) {
        model.addAttribute("requests", requestService.findAll());
        return "admin/requests";
    }

    @PostMapping("/requests/{id}/accept")
    public String acceptRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            requestService.approveRequest(id);
            redirectAttributes.addFlashAttribute("success", "Solicitud aceptada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al aceptar solicitud: " + e.getMessage());
        }
        return "redirect:/admin/requests";
    }

    @PostMapping("/requests/{id}/reject")
    public String rejectRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            requestService.rejectRequest(id);
            redirectAttributes.addFlashAttribute("success", "Solicitud rechazada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al rechazar solicitud: " + e.getMessage());
        }
        return "redirect:/admin/requests";
    }

    // CONFIGURATION MANAGEMENT
    @GetMapping("/config")
    public String showConfig(Model model) {
        model.addAttribute("heroImage", configService.getHeroImageUrl());
        return "admin/config";
    }

    @PostMapping("/config/hero-image")
    public String updateHeroImage(@RequestParam("heroImageFile") MultipartFile file,
                                RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor selecciona una imagen");
            return "redirect:/admin/config";
        }

        try {
            String imageUrl = cloudinaryService.uploadFile(file);
            configService.setHeroImageUrl(imageUrl);
            redirectAttributes.addFlashAttribute("success", "Imagen subida exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir imagen: " + e.getMessage());
        }
        
        return "redirect:/admin/config";
    }
}
