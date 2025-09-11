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
import java.util.Optional;

/**
 * Request Controller - Controlador unificado para gestión de solicitudes
 * 
 * Estructura:
 * - /requests - Endpoint base
 * - /requests/{id} - Detalle de solicitud
 * - /requests/new - Crear nueva solicitud
 * 
 * Prefijos por rol:
 * - /admin/requests - Admin (gestión completa)
 * - /user/requests - User (crear y ver propias)
 * - /org/requests - Organization (ver pendientes, aceptar/rechazar)
 */
@Controller
public class RequestController {

    @Autowired
    private RequestService requestService;
    
    @Autowired
    private UserService userService;

    // ========== MÉTODOS COMUNES ==========
    
    /**
     * Prepara modelo común para todas las vistas de solicitudes
     */
    private void prepareRequestModel(Model model, List<Request> requests, String viewType) {
        model.addAttribute("requests", requests);
        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("viewType", viewType);
    }
    
    /**
     * Maneja errores comunes en operaciones de solicitudes
     */
    private void handleRequestError(Exception e, RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message + ": " + e.getMessage());
    }

    // ========== ADMIN ENDPOINTS ==========
    
    /**
     * Lista todas las solicitudes (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/requests")
    public String adminRequests(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Long id,
            Model model) {
        
        // Modo edición
        if ("edit".equals(action) && id != null) {
            Optional<Request> requestOpt = requestService.findById(id);
            if (requestOpt.isPresent()) {
                model.addAttribute("request", requestOpt.get());
                model.addAttribute("viewType", "form");
                return "admin/requests";
            }
        }
        
        // Modo lista (por defecto)
        List<Request> allRequests = requestService.findAll();
        prepareRequestModel(model, allRequests, "list");
        return "admin/requests";
    }

    /**
     * Actualiza el estado de una solicitud (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/requests")
    public String adminUpdateRequest(
            @ModelAttribute Request request,
            RedirectAttributes redirectAttributes) {
        try {
            // Obtener solicitud original para preservar datos no modificables
            Optional<Request> originalRequest = requestService.findById(request.getId());
            if (originalRequest.isPresent()) {
                Request updatedRequest = originalRequest.get();
                updatedRequest.setStatus(request.getStatus());
                updatedRequest.setNotes(request.getNotes());
                requestService.save(updatedRequest);
                redirectAttributes.addFlashAttribute("successMessage", "Solicitud actualizada correctamente");
            }
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al actualizar solicitud");
        }
        return "redirect:/admin/requests";
    }

    /**
     * Actualiza estado específico de una solicitud (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/requests/update-status/{id}")
    public String adminUpdateRequestStatus(
            @PathVariable Long id, 
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            requestService.updateStatus(id, RequestStatus.valueOf(status));
            redirectAttributes.addFlashAttribute("successMessage", "Estado actualizado correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al actualizar estado");
        }
        return "redirect:/admin/requests";
    }

    /**
     * Elimina una solicitud (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/requests/delete/{id}")
    public String adminDeleteRequest(
            @PathVariable Long id, 
            RedirectAttributes redirectAttributes) {
        try {
            requestService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud eliminada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al eliminar solicitud");
        }
        return "redirect:/admin/requests";
    }

    // ========== USER ENDPOINTS ==========
    
    /**
     * Lista solicitudes del usuario actual
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/requests")
    public String userRequests(Authentication authentication, Model model) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            List<Request> userRequests = requestService.getRequestsByUser(currentUser);
            prepareRequestModel(model, userRequests, "list");
            return "users/requests";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar solicitudes: " + e.getMessage());
            return "users/requests";
        }
    }

    /**
     * Muestra formulario para nueva solicitud
     */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/requests/new")
    public String userNewRequestForm(Model model) {
        model.addAttribute("request", new Request());
        return "users/request-form";
    }

    /**
     * Crea una nueva solicitud
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/user/requests")
    public String userCreateRequest(
            @ModelAttribute Request request, 
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            request.setUser(currentUser);
            request.setStatus(RequestStatus.PENDING);
            requestService.save(request);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud creada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al crear solicitud");
        }
        return "redirect:/user/requests";
    }

    // ========== ORGANIZATION ENDPOINTS ==========
    
    /**
     * Lista solicitudes pendientes para organizaciones
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/org/requests")
    public String orgRequests(Model model) {
        List<Request> pendingRequests = requestService.getPendingRequests();
        prepareRequestModel(model, pendingRequests, "list");
        return "org/requests";
    }

    /**
     * Acepta una solicitud
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/org/requests/accept/{id}")
    public String orgAcceptRequest(
            @PathVariable Long id, 
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            // Asignar la organización actual a la solicitud
            User organization = userService.findAuthenticatedUserByUsername(authentication.getName());
            requestService.approveRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud aceptada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al aceptar solicitud");
        }
        return "redirect:/org/requests";
    }

    /**
     * Rechaza una solicitud
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/org/requests/reject/{id}")
    public String orgRejectRequest(
            @PathVariable Long id, 
            RedirectAttributes redirectAttributes) {
        try {
            requestService.rejectRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Solicitud rechazada correctamente");
        } catch (Exception e) {
            handleRequestError(e, redirectAttributes, "Error al rechazar solicitud");
        }
        return "redirect:/org/requests";
    }
}
