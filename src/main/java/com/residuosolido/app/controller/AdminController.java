package com.residuosolido.app.controller;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.repository.FeedbackRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final FeedbackRepository feedbackRepository;
    private final RequestRepository requestRepository;

    @Autowired
    public AdminController(DashboardService dashboardService, FeedbackRepository feedbackRepository, RequestRepository requestRepository) {
        this.dashboardService = dashboardService;
        this.feedbackRepository = feedbackRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = dashboardService.getAdminStats();
        model.addAllAttributes(stats);
        return "admin/dashboard";
    }
    
    @GetMapping("/feedback")
    public String listFeedback(Model model) {
        model.addAttribute("feedbacks", feedbackRepository.findAll());
        model.addAttribute("pageTitle", "Gestión de Feedback");
        return "admin/feedback";
    }
    
    @GetMapping("/feedback/delete/{id}")
    public String deleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        feedbackRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Feedback eliminado correctamente");
        return "redirect:/admin/feedback";
    }
    
    @GetMapping("/requests")
    public String listRequests(Model model) {
        List<Request> requests = requestRepository.findAll();
        model.addAttribute("requests", requests);
        model.addAttribute("pageTitle", "Gestión de Solicitudes");
        return "admin/requests";
    }
}
