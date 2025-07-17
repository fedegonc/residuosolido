package com.residuosolido.app.controller;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserRepository userRepository;
    
    @Autowired
    public FeedbackController(FeedbackService feedbackService, UserRepository userRepository) {
        this.feedbackService = feedbackService;
        this.userRepository = userRepository;
    }
    
    @GetMapping
    public String listAll(Model model) {
        model.addAttribute("feedbacks", feedbackService.findAll());
        return "feedback/list";
    }
    
    @GetMapping("/new")
    public String showFeedbackForm(Model model) {
        model.addAttribute("feedback", new Feedback());
        return "feedback/form";
    }
    
    @PostMapping("/save")
    public String saveFeedback(@ModelAttribute Feedback feedback, 
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            feedback.setUser(user);
            feedbackService.save(feedback);
            redirectAttributes.addFlashAttribute("successMessage", "¡Gracias por tu feedback!");
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Debes iniciar sesión para enviar feedback");
            return "redirect:/login";
        }
    }
    
    @GetMapping("/{id}")
    public String viewFeedback(@PathVariable("id") Long id, Model model) {
        Feedback feedback = feedbackService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback no encontrado"));
        model.addAttribute("feedback", feedback);
        return "feedback/detail";
    }
}
