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
                               @RequestParam(value = "q", required = false) String query,
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
        
        // Cargar y filtrar feedbacks (en memoria) según 'q'
        var all = feedbackService.findAll();
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            all = all.stream().filter(fb -> {
                String idStr = fb.getId() != null ? fb.getId().toString() : "";
                String userFullName = (fb.getUser() != null && fb.getUser().getFullName() != null) ? fb.getUser().getFullName().toLowerCase() : "";
                String userUsername = (fb.getUser() != null && fb.getUser().getUsername() != null) ? fb.getUser().getUsername().toLowerCase() : "";
                String userEmail = (fb.getUser() != null && fb.getUser().getEmail() != null) ? fb.getUser().getEmail().toLowerCase() : "";
                String name = fb.getName() != null ? fb.getName().toLowerCase() : "";
                String email = fb.getEmail() != null ? fb.getEmail().toLowerCase() : "";
                String comment = fb.getComment() != null ? fb.getComment().toLowerCase() : "";
                String created = fb.getCreatedAt() != null ? fb.getCreatedAt().toString().toLowerCase() : "";
                return idStr.contains(q) || userFullName.contains(q) || userUsername.contains(q) || userEmail.contains(q)
                        || name.contains(q) || email.contains(q) || comment.contains(q) || created.contains(q);
            }).toList();
        }
        model.addAttribute("feedbacks", all);
        model.addAttribute("feedback", feedback);
        model.addAttribute("viewType", viewType);
        model.addAttribute("totalFeedbacks", all != null ? all.size() : 0);
        model.addAttribute("query", query);
        model.addAttribute("users", userService.findAll()); // Para el selector de usuario en el formulario
        
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

    // ========== HTMX ENDPOINTS ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/feedback/form-demo")
    public String getFeedbackFormDemo(Model model) {
        Feedback demoFeedback = new Feedback();
        demoFeedback.setName("Juan Pérez");
        demoFeedback.setEmail("juan.perez@ejemplo.com");
        demoFeedback.setComment("Excelente aplicación, muy útil para gestionar residuos sólidos en la zona. Me gusta la interfaz intuitiva y la rapidez de las operaciones. Solo sugeriría agregar más opciones de filtrado en los reportes.");

        model.addAttribute("feedback", demoFeedback);
        model.addAttribute("users", userService.findAll()); // Para el selector de usuario
        return "admin/feedback :: feedbackFormFields";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/feedback/form-demo")
    public String createFeedbackDemo(@ModelAttribute Feedback feedback, RedirectAttributes redirectAttributes) {
        try {
            // Crear feedback de prueba con datos demo
            Feedback demoFeedback = new Feedback();
            demoFeedback.setName("Juan Pérez (Demo)");
            demoFeedback.setEmail("demo@ejemplo.com");
            demoFeedback.setComment("Este es un feedback de prueba creado desde el botón 'Completar campos'. Excelente aplicación, muy útil para gestionar residuos sólidos en la zona. Me gusta la interfaz intuitiva y la rapidez de las operaciones.");

            feedbackService.save(demoFeedback);

            redirectAttributes.addFlashAttribute("successMessage", "Feedback de prueba creado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear feedback de prueba: " + e.getMessage());
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
