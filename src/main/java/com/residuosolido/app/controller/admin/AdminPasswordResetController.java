package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.PasswordResetRequest;
import com.residuosolido.app.service.PasswordResetRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/password-reset-requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPasswordResetController {

    @Autowired
    private PasswordResetRequestService passwordResetService;

    @GetMapping
    public String list(@RequestParam(required = false) String action,
                       @RequestParam(required = false) Long id,
                       Model model) {
        List<PasswordResetRequest> all = passwordResetService.findAllOrderedByRequestDateDesc();
        model.addAttribute("passwordResetRequests", all);
        model.addAttribute("totalRequests", all != null ? all.size() : 0);
        model.addAttribute("statuses", PasswordResetRequest.Status.values());

        if ("edit".equals(action) && id != null) {
            Optional<PasswordResetRequest> request = passwordResetService.findById(id);
            request.ifPresent(r -> model.addAttribute("passwordResetRequest", r));
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", true);
            return "admin/password-reset-requests";
        }

        model.addAttribute("viewType", "list");
        return "admin/password-reset-requests";
    }

    @PostMapping
    public String save(@ModelAttribute PasswordResetRequest passwordResetRequest,
                       @RequestParam String status,
                       @RequestParam(required = false) String adminNotes,
                       RedirectAttributes ra) {
        try {
            if (passwordResetRequest.getId() != null) {
                // Editar solicitud existente (solo status y adminNotes)
                passwordResetService.findById(passwordResetRequest.getId()).ifPresent(existing -> {
                    try {
                        existing.setStatus(PasswordResetRequest.Status.valueOf(status.toUpperCase()));
                        if (adminNotes != null && !adminNotes.trim().isEmpty()) {
                            existing.setAdminNotes(adminNotes);
                        }
                        passwordResetService.save(existing);
                    } catch (IllegalArgumentException e) {
                        // Si el estado no es válido, mantener el actual
                    }
                });
                ra.addFlashAttribute("successMessage", "Solicitud de reset actualizada correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/password-reset-requests";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            passwordResetService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Solicitud de reset eliminada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/password-reset-requests";
    }
}
