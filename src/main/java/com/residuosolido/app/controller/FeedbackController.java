package com.residuosolido.app.controller;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.FeedbackRepository;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public String showFeedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        return "feedback/form";
    }
    
    @PostMapping
    public String saveFeedback(@ModelAttribute Feedback feedback, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            logger.info("Intentando guardar feedback para usuario: {}", authentication.getName());
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            
            // Llenar automáticamente los datos del usuario
            feedback.setUser(user);
            
            // Construir nombre completo manejando valores nulos
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            String fullName = (firstName + " " + lastName).trim();
            feedback.setName(fullName.isEmpty() ? user.getUsername() : fullName);
            
            // Manejar email nulo
            feedback.setEmail(user.getEmail() != null ? user.getEmail() : "");
            
            feedbackRepository.save(feedback);
            logger.info("Feedback guardado exitosamente");
            redirectAttributes.addFlashAttribute("successMessage", "¡Gracias por tu feedback!");
        } catch (Exception e) {
            logger.error("Error al guardar feedback: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error al enviar feedback: " + e.getMessage());
        }
        return "redirect:/feedback";
    }
}
