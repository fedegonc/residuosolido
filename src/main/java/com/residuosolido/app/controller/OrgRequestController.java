package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.service.RequestOrganizationService;
import com.residuosolido.app.service.RequestTransitionService;
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

@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgRequestController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrgRequestController.class);

    private final RequestOrganizationService requestOrganizationService;
    private final RequestTransitionService requestTransitionService;

    @Autowired
    public OrgRequestController(RequestOrganizationService requestOrganizationService,
                               RequestTransitionService requestTransitionService) {
        this.requestOrganizationService = requestOrganizationService;
        this.requestTransitionService = requestTransitionService;
    }

    @GetMapping("/acopio/requests")
    public String orgRequests(@RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication, Model model) {
        User currentOrg = getCurrentUser(authentication);
        List<Request> requests = requestOrganizationService.getOrgRequestsByStatusFilter(currentOrg, status, page, size);

        model.addAttribute("requests", requests);
        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("viewType", "list");
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "org/requests";
    }

    @PostMapping("/acopio/requests/{id}/transition")
    public String orgTransitionRequest(@PathVariable String id,
                                       @RequestParam("action") String action,
                                       @RequestParam(value = "confirmedSlot", required = false) TimeSlot confirmedSlot,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        try {
            User org = getCurrentUser(authentication);
            switch (action) {
                case "accept" -> {
                    requestTransitionService.acceptRequest(id, org, confirmedSlot);
                    flashSuccess(redirectAttributes, "flash.org.request_accepted");
                }
                case "reject" -> {
                    requestTransitionService.rejectRequest(id, org);
                    flashSuccess(redirectAttributes, "flash.org.request_rejected");
                }
                case "complete" -> {
                    requestTransitionService.completeRequest(id, org);
                    flashSuccess(redirectAttributes, "flash.org.request_completed");
                    return "redirect:/acopio/inicio";
                }
                default -> flashError(redirectAttributes, "flash.org.request_invalid_action");
            }
        } catch (SecurityException e) {
            flashError(redirectAttributes, "flash.org.request_not_owned");
        } catch (Exception e) {
            logger.error("Error en transición '{}' para solicitud {}: {}", action, id, e.getMessage(), e);
            flashError(redirectAttributes, "flash.org.request_transition_error");
        }
        return "redirect:/acopio/requests";
    }
}
