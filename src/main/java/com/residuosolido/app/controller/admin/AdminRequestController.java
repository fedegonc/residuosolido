package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/requests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRequestController {

    @Autowired
    private RequestService requestService;

    @GetMapping
    public String list(@RequestParam(required = false) String action,
                       @RequestParam(required = false) Long id,
                       Model model) {
        List<Request> all = requestService.findAll();
        model.addAttribute("requests", all);
        model.addAttribute("totalRequests", all != null ? all.size() : 0);

        if ("edit".equals(action) && id != null) {
            Optional<Request> request = requestService.findById(id);
            request.ifPresent(r -> model.addAttribute("request", r));
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", true);
            return "admin/requests";
        }

        model.addAttribute("viewType", "list");
        return "admin/requests";
    }

    @PostMapping
    public String save(@ModelAttribute Request request,
                       @RequestParam String status,
                       @RequestParam(required = false) String notes,
                       RedirectAttributes ra) {
        try {
            if (request.getId() != null) {
                // Editar solicitud existente (solo status y notes)
                requestService.findById(request.getId()).ifPresent(existing -> {
                    try {
                        existing.setStatus(Request.RequestStatus.valueOf(status.toUpperCase()));
                        if (notes != null && !notes.trim().isEmpty()) {
                            existing.setNotes(notes);
                        }
                        requestService.save(existing);
                    } catch (IllegalArgumentException e) {
                        // Si el estado no es válido, mantener el actual
                    }
                });
                ra.addFlashAttribute("successMessage", "Solicitud actualizada correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/requests";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            requestService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Solicitud eliminada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/requests";
    }
}
