package com.residuosolido.app.controller;

import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.RequestOrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgRequestDetailController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrgRequestDetailController.class);

    private final RequestOrganizationService requestOrganizationService;

    @Autowired
    public OrgRequestDetailController(RequestOrganizationService requestOrganizationService) {
        this.requestOrganizationService = requestOrganizationService;
    }

    @GetMapping("/acopio/requests/{id}")
    public String orgRequestDetail(@PathVariable String id, Authentication authentication,
                                    Model model, RedirectAttributes redirectAttributes) {
        try {
            User org = getCurrentUser(authentication);
            Request request = requestOrganizationService.getOwnedOrgRequest(id, org);
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
}
