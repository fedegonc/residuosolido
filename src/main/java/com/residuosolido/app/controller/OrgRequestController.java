package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.service.UserService;
import com.residuosolido.app.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgRequestController {

    private static final Logger logger = LoggerFactory.getLogger(OrgRequestController.class);

    private final UserService userService;
    private final RequestService requestService;
    private final MessageSource messageSource;

    @Autowired
    public OrgRequestController(UserService userService, RequestService requestService, MessageSource messageSource) {
        this.userService = userService;
        this.requestService = requestService;
        this.messageSource = messageSource;
    }

    @GetMapping("/acopio/requests")
    public String orgRequests(@RequestParam(required = false) String status,
            Authentication authentication, Model model) {
        User currentOrg = userService.findAuthenticatedUserByUsername(authentication.getName());
        List<Request> requests = requestService.getOrgRequestsByStatusFilter(currentOrg, status);

        model.addAttribute("requests", requests);
        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("viewType", "list");
        model.addAttribute("currentStatus", status);
        return "org/requests";
    }

    @GetMapping("/acopio/requests/{id}")
    public String orgRequestDetail(@PathVariable String id, Authentication authentication,
                                    Model model, RedirectAttributes redirectAttributes) {
        try {
            User org = userService.findAuthenticatedUserByUsername(authentication.getName());
            Request request = requestService.getOwnedOrgRequest(id, org);
            model.addAttribute("request", request);
            model.addAttribute("viewType", "detail");
            model.addAttribute("timeSlots", TimeSlot.values());
            return "org/requests";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.org.request_not_owned", null, LocaleContextHolder.getLocale()));
            return "redirect:/acopio/requests";
        } catch (Exception e) {
            logger.error("Error al cargar solicitud {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.org.request_load_error", null, LocaleContextHolder.getLocale()));
            return "redirect:/acopio/requests";
        }
    }

    @PostMapping("/acopio/requests/accept/{id}")
    public String orgAcceptRequest(@PathVariable String id,
                                   @RequestParam("confirmedSlot") TimeSlot confirmedSlot,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            User org = userService.findAuthenticatedUserByUsername(authentication.getName());
            requestService.acceptRequest(id, org, confirmedSlot);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.org.request_accepted", null, LocaleContextHolder.getLocale()));
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.org.request_not_owned", null, LocaleContextHolder.getLocale()));
            return "redirect:/acopio/requests";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.org.request_accept_error", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/acopio/inicio";
    }

    @PostMapping("/acopio/requests/reject/{id}")
    public String orgRejectRequest(@PathVariable String id, Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            User org = userService.findAuthenticatedUserByUsername(authentication.getName());
            requestService.rejectRequest(id, org);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.org.request_rejected", null, LocaleContextHolder.getLocale()));
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.org.request_not_owned", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.org.request_reject_error", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/acopio/requests";
    }

    @PostMapping("/acopio/requests/complete/{id}")
    public String orgCompleteRequest(@PathVariable String id, Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            User org = userService.findAuthenticatedUserByUsername(authentication.getName());
            requestService.completeRequest(id, org);
            redirectAttributes.addFlashAttribute("successMessage", messageSource.getMessage("flash.org.request_completed", null, LocaleContextHolder.getLocale()));
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.org.request_not_owned", null, LocaleContextHolder.getLocale()));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.org.request_complete_error", null, LocaleContextHolder.getLocale()));
        }
        return "redirect:/acopio/inicio";
    }
}
