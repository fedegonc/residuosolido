package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * Gestión de solicitudes por la organización:
 * - Lista con filtro por estado
 * - Detalle individual
 * - Transiciones de estado (aceptar, rechazar, completar)
 *
 * Antes estaba dividido en OrgRequestController + OrgRequestDetailController.
 */
@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgRequestController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrgRequestController.class);

    private final RequestService requestService;

    @Autowired
    public OrgRequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    /** Lista las solicitudes de la organización, con filtro opcional por estado. */
    @GetMapping(Routes.ORG_REQUESTS)
    public String orgRequests(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication, Model model) {
        User currentOrg = getCurrentUser(authentication);
        List<Request> requests = requestService.getOrgRequestsByStatusFilter(currentOrg, status, page, size);

        model.addAttribute("requests", requests);
        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("viewType", "list");
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("breadcrumbs", List.of(
                Map.of("label", "Inicio", "href", "/"),
                Map.of("label", "Panel de acopio", "href", "/acopio/inicio"),
                Map.of("label", "Solicitudes", "href", "")
        ));
        return "org/requests";
    }

    /** Carga una solicitud individual con sus datos completos. */
    @GetMapping(Routes.ORG_REQUEST)
    public String orgRequestDetail(@PathVariable String id, Authentication authentication,
                                    Model model, RedirectAttributes redirectAttributes) {
        try {
            User org = getCurrentUser(authentication);
            Request request = requestService.getOwnedOrgRequest(id, org);
            model.addAttribute("request", request);
            model.addAttribute("viewType", "detail");
            model.addAttribute("timeSlots", TimeSlot.values());
            return "org/requests";
        } catch (SecurityException e) {
            flashError(redirectAttributes, "flash.org.request_not_owned");
            return "redirect:/acopio/requests";
        } catch (Exception e) {
            logger.error("Error al cargar solicitud {}: {}", id, e.getMessage(), e);
            flashError(redirectAttributes, "flash.org.request_load_error");
            return "redirect:/acopio/requests";
        }
    }

    /** Cambia el estado de una solicitud: aceptar, rechazar o completar. */
    @PostMapping(Routes.ORG_REQUEST_TRANSITION)
    public String orgTransitionRequest(@PathVariable String id,
                                       @RequestParam("action") String action,
                                       @RequestParam(value = "confirmedSlot", required = false) TimeSlot confirmedSlot,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        try {
            User org = getCurrentUser(authentication);
            switch (action) {
                case "accept" -> {
                    requestService.acceptRequest(id, org, confirmedSlot);
                    flashSuccess(redirectAttributes, "flash.org.request_accepted");
                }
                case "reject" -> {
                    requestService.rejectRequest(id, org);
                    flashSuccess(redirectAttributes, "flash.org.request_rejected");
                }
                case "complete" -> {
                    requestService.completeRequest(id, org);
                    flashSuccess(redirectAttributes, "flash.org.request_completed");
                    return "redirect:/acopio/inicio";
                }
                default -> flashError(redirectAttributes, "flash.org.request_invalid_action");
            }
        } catch (SecurityException e) {
            flashError(redirectAttributes, "flash.org.request_not_owned");
        } catch (IllegalStateException e) {
            flashError(redirectAttributes, e.getMessage());
        } catch (Exception e) {
            logger.error("Error en transición '{}' para solicitud {}: {}", action, id, e.getMessage(), e);
            flashError(redirectAttributes, "flash.org.request_transition_error");
        }
        return "redirect:/acopio/requests";
    }
}
