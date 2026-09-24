package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.enums.RequestViewType;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.util.LandingCardLoader;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Gestión de solicitudes por la organización:
 * - Lista con filtro por estado (pantalla principal del acopio)
 * - Detalle individual
 * - Transiciones de estado (aceptar, rechazar, completar)
 *
 * Antes estaba dividido en OrgRequestController + OrgRequestDetailController.
 */
@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgRequestController {

    private final RequestMetricsService requestMetricsService;
    private final RequestService requestService;
    private final LocaleResolver localeResolver;
    private final Messages messages;

    public OrgRequestController(RequestMetricsService requestMetricsService,
                                RequestService requestService,
                                LocaleResolver localeResolver,
                                Messages messages) {
        this.requestMetricsService = requestMetricsService;
        this.requestService = requestService;
        this.localeResolver = localeResolver;
        this.messages = messages;
    }

    /** Lista las solicitudes de la organización, con filtro opcional por estado. */
    @GetMapping(Routes.ORG_REQUESTS)
    public String orgRequests(@RequestParam(value = "estado", required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser User currentOrg, Model model,
            HttpServletRequest request) {
        if (currentOrg.needsProfileCompletion()) {
            return "redirect:" + Routes.ORG_PROFILE;
        }

        Map<String, Long> stats = requestMetricsService.getOrgRequestStats(currentOrg);
        model.addAttribute("pendingCount", stats.get("pending"));
        model.addAttribute("inProgressCount", stats.get("inProgress"));
        model.addAttribute("completedCount", stats.get("completed"));

        List<Request> requests = requestService.getOrgRequestsByStatusFilter(currentOrg, estado, page, size);

        model.addAttribute("requests", requests);
        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("viewType", RequestViewType.LIST);
        model.addAttribute("currentStatus", estado);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("breadcrumbs", List.of(
                Map.of("label", "Inicio", "href", "/"),
                Map.of("label", "Panel de acopio", "href", "")
        ));
        Locale locale = localeResolver.resolveLocale(request);
        model.addAttribute("cards", LandingCardLoader.loadCards(locale.getLanguage()));
        return "org/requests";
    }

    /** Carga una solicitud individual con sus datos completos. */
    @GetMapping(Routes.ORG_REQUEST)
    public String orgRequestDetail(@PathVariable String id, @CurrentUser User org,
                                    Model model) {
        Request request = requestService.getOwnedOrgRequest(id, org);
        model.addAttribute("request", request);
        model.addAttribute("viewType", RequestViewType.DETAIL);
        model.addAttribute("timeSlots", TimeSlot.values());
        return "org/requests";
    }

    /** Acepta una solicitud pendiente, opcionalmente confirmando el horario. */
    @PostMapping(Routes.ORG_REQUEST_ACCEPT)
    public String acceptRequest(@PathVariable String id,
                                @RequestParam(value = "confirmedSlot", required = false) TimeSlot confirmedSlot,
                                @CurrentUser User org,
                                RedirectAttributes redirectAttributes) {
        try {
            requestService.acceptRequest(id, org, confirmedSlot);
            messages.flashSuccess(redirectAttributes, ServerMessage.FLASH_ORG_REQUEST_ACCEPTED);
        } catch (IllegalStateException e) {
            messages.flashError(redirectAttributes, e);
        }
        return "redirect:" + Routes.ORG_REQUESTS;
    }

    /** Rechaza una solicitud pendiente. */
    @PostMapping(Routes.ORG_REQUEST_REJECT)
    public String rejectRequest(@PathVariable String id,
                                @CurrentUser User org,
                                RedirectAttributes redirectAttributes) {
        try {
            requestService.rejectRequest(id, org);
            messages.flashSuccess(redirectAttributes, ServerMessage.FLASH_ORG_REQUEST_REJECTED);
        } catch (IllegalStateException e) {
            messages.flashError(redirectAttributes, e);
        }
        return "redirect:" + Routes.ORG_REQUESTS;
    }

    /** Marca una solicitud aceptada como completada. */
    @PostMapping(Routes.ORG_REQUEST_COMPLETE)
    public String completeRequest(@PathVariable String id,
                                  @CurrentUser User org,
                                  RedirectAttributes redirectAttributes) {
        try {
            requestService.completeRequest(id, org);
            messages.flashSuccess(redirectAttributes, ServerMessage.FLASH_ORG_REQUEST_COMPLETED);
        } catch (IllegalStateException e) {
            messages.flashError(redirectAttributes, e);
        }
        return "redirect:" + Routes.ORG_REQUESTS;
    }

    /** Toda operación de este controller que falle por no ser dueño de la solicitud cae acá. */
    @ExceptionHandler(SecurityException.class)
    public String handleNotOwned(RedirectAttributes redirectAttributes) {
        messages.flashError(redirectAttributes, ServerMessage.FLASH_ORG_REQUEST_NOT_OWNED);
        return "redirect:" + Routes.ORG_REQUESTS;
    }
}
