package com.residuosolido.app.controller;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.FeedbackService;
import com.residuosolido.app.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    public FeedbackController(FeedbackService feedbackService, UserService userService) {
        this.feedbackService = feedbackService;
        this.userService = userService;
    }

    @GetMapping("/feedback")
    public String showForm(Model model, Authentication authentication) {
        if (!model.containsAttribute("feedback")) {
            model.addAttribute("feedback", new Feedback());
        }
        // Info del usuario autenticado para el formulario/vista si se requiere
        if (authentication != null) {
            User currentUser = userService.findByUsername(authentication.getName());
            model.addAttribute("currentUser", currentUser);
        }
        return "feedback/form";
    }

    @PostMapping("/feedback")
    public String submitFeedback(@ModelAttribute("feedback") Feedback feedback,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (authentication != null) {
                User currentUser = userService.findByUsername(authentication.getName());
                feedback.setUser(currentUser);
                // Completar nombre/email si es posible
                if (feedback.getName() == null || feedback.getName().isBlank()) {
                    String fullName = (currentUser.getFirstName() != null ? currentUser.getFirstName() : "")
                            + (currentUser.getLastName() != null ? (" " + currentUser.getLastName()) : "");
                    feedback.setName(fullName.isBlank() ? currentUser.getUsername() : fullName.trim());
                }
                if (feedback.getEmail() == null || feedback.getEmail().isBlank()) {
                    feedback.setEmail(currentUser.getEmail());
                }
            }

            if (feedback.getComment() == null || feedback.getComment().isBlank()) {
                redirectAttributes.addFlashAttribute("errorMessage", "El comentario no puede estar vacío.");
                redirectAttributes.addFlashAttribute("feedback", feedback);
                return "redirect:/feedback";
            }

            feedbackService.save(feedback);
            redirectAttributes.addFlashAttribute("successMessage", "¡Gracias por tu feedback! Lo hemos recibido correctamente.");
            return "redirect:/feedback";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ocurrió un error al enviar tu feedback.");
            redirectAttributes.addFlashAttribute("feedback", feedback);
            return "redirect:/feedback";
        }
    }
}
