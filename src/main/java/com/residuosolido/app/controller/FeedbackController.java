package com.residuosolido.app.controller;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.FeedbackService;
import com.residuosolido.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Feedback Controller - Maneja feedback por rol
 * Endpoints:
 * - /admin/feedback → ROLE_ADMIN (gestión completa)
 * - /feedback → Usuarios logueados (crear feedback)
 */
@Controller
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private UserService userService;

    // ========== ADMIN ENDPOINTS ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/feedback")
    public String adminFeedback(@RequestParam(value = "action", required = false) String action,
                               @RequestParam(value = "id", required = false) Long id,
                               Model model) {
        
        String viewType = "list";
        Feedback feedback = new Feedback();
        
        if ("new".equals(action)) {
            viewType = "form";
            // feedback ya está inicializado
        } else if ("edit".equals(action) && id != null) {
            viewType = "form";
            feedback = feedbackService.findById(id).orElse(new Feedback());
        } else if ("view".equals(action) && id != null) {
            viewType = "view";
            feedback = feedbackService.findById(id).orElse(new Feedback());
        } else if ("respond".equals(action) && id != null) {
            viewType = "respond";
            feedback = feedbackService.findById(id).orElse(new Feedback());
        }
        
        model.addAttribute("feedbacks", feedbackService.findAll());
        model.addAttribute("feedback", feedback);
        model.addAttribute("viewType", viewType);
        model.addAttribute("totalFeedbacks", feedbackService.count());
        
        return "admin/feedback";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/feedback")
    public String adminSaveFeedback(@RequestParam("action") String action,
                                   @ModelAttribute Feedback feedback,
                                   RedirectAttributes redirectAttributes) {
        try {
            if ("delete".equals(action)) {
                feedbackService.deleteById(feedback.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Feedback eliminado correctamente");
            } else if ("respond".equals(action)) {
                feedbackService.respondToFeedback(feedback.getId(), feedback.getAdminResponse());
                redirectAttributes.addFlashAttribute("successMessage", "Respuesta enviada correctamente");
            } else {
                feedbackService.save(feedback);
                String message = feedback.getId() == null ? "Feedback creado correctamente" : "Feedback actualizado correctamente";
                redirectAttributes.addFlashAttribute("successMessage", message);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar feedback: " + e.getMessage());
        }
        
        return "redirect:/admin/feedback";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/feedback/mark-read/{id}")
    public String adminMarkFeedbackRead(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            feedbackService.markAsRead(id);
            redirectAttributes.addFlashAttribute("successMessage", "Feedback marcado como leído");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al marcar feedback");
        }
        return "redirect:/admin/feedback";
    }

    // ========== USER ENDPOINTS ==========
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/feedback")
    public String userFeedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        return "public/feedback-form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/feedback")
    public String userSubmitFeedback(@ModelAttribute Feedback feedback,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            feedback.setUser(currentUser);
            feedbackService.save(feedback);
            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Gracias por tu feedback! Hemos recibido tu mensaje y te responderemos pronto.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al enviar feedback. Inténtalo de nuevo.");
        }
        
        return "redirect:/feedback";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/feedback/my")
    public String userMyFeedback(Authentication authentication, Model model) {
        User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
        model.addAttribute("feedbacks", feedbackService.findByUser(currentUser));
        return "public/my-feedback";
    }
}
