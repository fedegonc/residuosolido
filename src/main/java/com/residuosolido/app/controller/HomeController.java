package com.residuosolido.app.controller;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.repository.MaterialRepository;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.NotificationRepository;
import com.residuosolido.app.service.FeedbackService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final OrganizationRepository organizationRepository;
    private final RequestRepository requestRepository;
    private final NotificationRepository notificationRepository;
    private final FeedbackService feedbackService;

    public HomeController(
            UserRepository userRepository, 
            MaterialRepository materialRepository,
            OrganizationRepository organizationRepository,
            RequestRepository requestRepository,
            NotificationRepository notificationRepository,
            FeedbackService feedbackService) {
        this.userRepository = userRepository;
        this.materialRepository = materialRepository;
        this.organizationRepository = organizationRepository;
        this.requestRepository = requestRepository;
        this.notificationRepository = notificationRepository;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Obtener todos los datos de las entidades
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("materials", materialRepository.findAll());
        model.addAttribute("organizations", organizationRepository.findAll());
        model.addAttribute("requests", requestRepository.findAll());
        model.addAttribute("notifications", notificationRepository.findAll());
        model.addAttribute("feedbacks", feedbackService.findAll());
        
        // Agregar el usuario actual si está autenticado
        if (userDetails != null) {
            model.addAttribute("currentUser", userDetails.getUsername());
        }
        
        return "index";
    }
}
