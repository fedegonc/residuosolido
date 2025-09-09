package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Request Controller - Maneja solicitudes de recolección por rol
 * Endpoints:
 * - /admin/requests → ROLE_ADMIN (gestión completa)
 * - /user/requests → ROLE_USER (crear y ver propias)
 * - /org/requests → ROLE_ORGANIZATION (ver asignadas)
 */
@Controller
public class RequestController {

    @Autowired
    private RequestService requestService;
    
    @Autowired
    private UserService userService;

    // ========== ADMIN ENDPOINTS ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/requests")
    public String adminRequests(Model model) {
        model.addAttribute("requests", requestService.findAll());
        model.addAttribute("totalRequests", requestService.count());
        return "admin/requests";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/requests/update-status/{id}")
    public String adminUpdateRequestStatus(@PathVariable Long id, 
                                          @RequestParam String status,
                                          RedirectAttributes redirectAttributes) {
        try {
            requestService.updateStatus(id, RequestStatus.valueOf(status));
            redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar estado");
        }
        return "redirect:/admin/requests";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/requests/delete/{id}")
    public String adminDeleteRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            requestService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud eliminada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar solicitud");
        }
        return "redirect:/admin/requests";
    }

    // ========== USER ENDPOINTS ==========
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/requests")
    public String userRequests(Authentication authentication, Model model) {
        User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
        List<Request> userRequests = requestService.getRequestsByUser(currentUser);
        
        model.addAttribute("requests", userRequests);
        model.addAttribute("totalRequests", userRequests.size());
        return "user/requests";
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/requests/new")
    public String userNewRequestForm(Model model) {
        model.addAttribute("request", new Request());
        return "user/request-form";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/user/requests")
    public String userCreateRequest(@ModelAttribute Request request, 
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            request.setUser(currentUser);
            requestService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud creada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al crear solicitud");
        }
        return "redirect:/user/requests";
    }

    // ========== ORGANIZATION ENDPOINTS ==========
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/org/requests")
    public String orgRequests(Model model) {
        List<Request> pendingRequests = requestService.getPendingRequests();
        model.addAttribute("requests", pendingRequests);
        model.addAttribute("totalRequests", pendingRequests.size());
        return "org/requests";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/org/requests/accept/{id}")
    public String orgAcceptRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            requestService.approveRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud aceptada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al aceptar solicitud");
        }
        return "redirect:/org/requests";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/org/requests/reject/{id}")
    public String orgRejectRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            requestService.rejectRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud rechazada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al rechazar solicitud");
        }
        return "redirect:/org/requests";
    }
}
