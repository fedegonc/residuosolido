package com.residuosolido.app.controller;

import com.residuosolido.app.model.Feedback;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.PasswordResetRequest;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.FeedbackRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.service.DashboardService;
import com.residuosolido.app.service.PasswordResetService;
import com.residuosolido.app.service.UserService;
import com.residuosolido.app.service.WasteSectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final FeedbackRepository feedbackRepository;
    private final RequestRepository requestRepository;
    private final PasswordResetService passwordResetService;
    private final UserService userService;
    private final WasteSectionService wasteSectionService;

    @Autowired
    public AdminController(DashboardService dashboardService, FeedbackRepository feedbackRepository, 
                          RequestRepository requestRepository, PasswordResetService passwordResetService,
                          UserService userService, WasteSectionService wasteSectionService) {
        this.dashboardService = dashboardService;
        this.feedbackRepository = feedbackRepository;
        this.requestRepository = requestRepository;
        this.passwordResetService = passwordResetService;
        this.userService = userService;
        this.wasteSectionService = wasteSectionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = dashboardService.getAdminStats();
        model.addAllAttributes(stats);
        return "admin/dashboard";
    }
    
    @GetMapping("/password-reset-requests")
    public String listPasswordResetRequests(Model model) {
        List<PasswordResetRequest> requests = passwordResetService.getPendingRequests();
        model.addAttribute("requests", requests);
        model.addAttribute("pageTitle", "Solicitudes de Recuperación de Contraseña");
        return "admin/password-reset-requests";
    }
    
    @GetMapping("/password-reset-requests/{id}/approve")
    public String approvePasswordReset(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        passwordResetService.approveRequest(id, "Aprobado por administrador");
        redirectAttributes.addFlashAttribute("success", "Solicitud aprobada. Contacta al usuario manualmente.");
        return "redirect:/admin/password-reset-requests";
    }
    
    @GetMapping("/password-reset-requests/{id}/reject")
    public String rejectPasswordReset(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        passwordResetService.rejectRequest(id, "Rechazado por administrador");
        redirectAttributes.addFlashAttribute("error", "Solicitud rechazada.");
        return "redirect:/admin/password-reset-requests";
    }
    
    @GetMapping("/api/notifications")
    @ResponseBody
    public Map<String, Object> getNotifications() {
        List<PasswordResetRequest> pendingRequests = passwordResetService.getPendingRequests();
        
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> notifications = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (PasswordResetRequest request : pendingRequests) {
            Map<String, String> notification = new HashMap<>();
            notification.put("title", "Nueva solicitud de recuperación");
            notification.put("message", "Email: " + request.getMaskedEmail());
            notification.put("time", request.getRequestDate().format(formatter));
            notifications.add(notification);
        }
        
        response.put("count", pendingRequests.size());
        response.put("notifications", notifications);
        
        return response;
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
    
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        model.addAttribute("pageTitle", "Gestión de Usuarios");
        return "admin/users";
    }
    
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findUserById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
                return "redirect:/admin/users";
            }
            
            // Verificar que no sea el último administrador
            if (user.getRole().name().equals("ADMIN")) {
                long adminCount = userService.countByRole(com.residuosolido.app.model.Role.ADMIN);
                if (adminCount <= 1) {
                    redirectAttributes.addFlashAttribute("error", "No se puede eliminar el último administrador del sistema.");
                    return "redirect:/admin/users";
                }
            }
            
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    // Waste Sections Management
    @GetMapping("/waste-sections")
    public String wasteSections(Model model) {
        model.addAttribute("wasteSections", wasteSectionService.findAll());
        return "admin/waste-sections";
    }
}
