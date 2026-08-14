package com.residuosolido.app.controller;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.RequestQueryService;
import com.residuosolido.app.service.RequestUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Operaciones del ciudadano: listar, ver detalle y eliminar sus solicitudes. */
@Controller
public class RequestController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);

    private final RequestUpdateService requestUpdateService;
    private final RequestQueryService requestQueryService;

    @Autowired
    public RequestController(RequestUpdateService requestUpdateService,
                             RequestQueryService requestQueryService) {
        this.requestUpdateService = requestUpdateService;
        this.requestQueryService = requestQueryService;
    }

    /** Página de confirmación tras crear una solicitud. */
    @GetMapping("/solicitudes/exito")
    public String requestSuccess(@RequestParam(value = "id", required = false) String id,
                                  Model model, Authentication authentication) {
        if (id != null && !id.isBlank()) {
            model.addAttribute("createdRequestId", id);
            model.addAttribute("createdRequestStatus", "PENDING");
        }
        // isGuest normalmente llega como flash attribute desde RequestCreateController,
        // pero ese flash solo sobrevive un redirect: si el usuario refresca la página,
        // cambia de idioma o navega directo, hay que recalcularlo desde la sesión actual.
        if (!model.containsAttribute("isGuest")) {
            model.addAttribute("isGuest", userService.isAnonymous(authentication));
        }
        return "users/request-success";
    }

    /** Lista las solicitudes del usuario autenticado (paginado). */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/solicitudes")
    public String listUserRequests(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size,
                                    Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        model.addAttribute("requests", requestQueryService.getRequestsByUser(user, page, size));
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "users/requests";
    }

    /** Muestra el detalle de una solicitud del usuario. */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/solicitud/{id}")
    public String requestDetail(@PathVariable String id, Authentication authentication, Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            Request request = requestQueryService.getOwnedRequest(id, user);
            model.addAttribute("request", request);
            return "users/request-detail";
        } catch (SecurityException e) {
            flashError(redirectAttributes, "flash.request.not_owned");
            return "redirect:/solicitudes";
        } catch (Exception e) {
            flashError(redirectAttributes, "flash.request.load_error");
            return "redirect:/solicitudes";
        }
    }

    /** Elimina una solicitud del usuario (solo si está pendiente). */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/solicitud/{id}/eliminar")
    public String deleteRequest(@PathVariable String id, Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(authentication);
            requestUpdateService.deleteOwnedRequest(id, user);
            flashSuccess(redirectAttributes, "flash.request.deleted");
        } catch (SecurityException e) {
            flashError(redirectAttributes, "flash.request.not_owned");
        } catch (Exception e) {
            logger.error("Error al eliminar solicitud: {}", e.getMessage());
            flashError(redirectAttributes, "flash.request.delete_error");
        }
        return "redirect:/solicitudes";
    }
}
