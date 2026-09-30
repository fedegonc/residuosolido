package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.enums.RequestStatus;
import com.residuosolido.app.enums.RequestViewType;
import com.residuosolido.app.enums.TimeSlot;
import com.residuosolido.app.service.MonthlyReportService;
import com.residuosolido.app.service.OrganizationService;
import com.residuosolido.app.service.OrgRequestPdfService;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestService;
import com.residuosolido.app.util.LandingCardLoader;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

    private static final Logger logger = LoggerFactory.getLogger(OrgRequestController.class);
    private final RequestMetricsService requestMetricsService;
    private final RequestService requestService;
    private final OrganizationService organizationService;
    private final LocaleResolver localeResolver;
    private final Messages messages;
    private final OrgRequestPdfService pdfService;
    private final MonthlyReportService monthlyReportService;

    public OrgRequestController(RequestMetricsService requestMetricsService,
                                RequestService requestService,
                                OrganizationService organizationService,
                                LocaleResolver localeResolver,
                                Messages messages,
                                OrgRequestPdfService pdfService,
                                MonthlyReportService monthlyReportService) {
        this.requestMetricsService = requestMetricsService;
        this.requestService = requestService;
        this.organizationService = organizationService;
        this.localeResolver = localeResolver;
        this.messages = messages;
        this.pdfService = pdfService;
        this.monthlyReportService = monthlyReportService;
    }

    /** Lista las solicitudes de la organización como tablero Kanban por estado. */
    @GetMapping(Routes.ORG_REQUESTS)
    public String orgRequests(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @CurrentUser User currentOrg, Model model,
            HttpServletRequest request) {
        logger.info("🔍 OrgRequestController.orgRequests() — usuario: {}", currentOrg.getUsername());
        Organization organization = organizationService.findByUser(currentOrg);
        logger.info("📦 Organización: id={}, name={}, profileCompleted={}, hasPhone={}, hasCity={}",
                organization.getId(), organization.getName(), organization.isProfileComplete(),
                organization.hasPhone(), organization.hasCity());
        model.addAttribute("organization", organization);
        if (organization.needsProfileCompletion()) {
            logger.warn("⚠️  Perfil incompleto — mostrando advertencia");
            model.addAttribute("warningMessage", "Completa tu perfil para recibir solicitudes");
        }

        Map<String, Long> stats = requestMetricsService.getOrgRequestStats(organization);
        long pending = stats.get("pending");
        long inProgress = stats.get("inProgress");
        long completed = stats.get("completed");
        long rejected = stats.get("rejected");
        model.addAttribute("pendingCount", pending);
        model.addAttribute("inProgressCount", inProgress);
        model.addAttribute("completedCount", completed);
        model.addAttribute("rejectedCount", rejected);

        List<Request> requests = requestService.getRequestsByOrganization(organization, page, size);

        model.addAttribute("requests", requests);
        model.addAttribute("requestsByStatus", groupByStatus(requests));
        model.addAttribute("totalRequests", requests.size());
        model.addAttribute("viewType", RequestViewType.LIST);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("breadcrumbs", List.of(
                Map.of("label", "Inicio", "href", "/"),
                Map.of("label", "Panel de acopio", "href", "")
        ));
        Locale locale = localeResolver.resolveLocale(request);
        model.addAttribute("cards", LandingCardLoader.loadCards(locale.getLanguage()));
        logger.info("✅ Renderizando org/requests.html con {} requests", requests.size());
        return "org/requests";
    }

    /** Carga una solicitud individual con sus datos completos. */
    @GetMapping(Routes.ORG_REQUEST)
    public String orgRequestDetail(@PathVariable String id, @CurrentUser User currentOrg,
                                    Model model) {
        Organization org = organizationService.findByUser(currentOrg);
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
                                @CurrentUser User currentOrg,
                                RedirectAttributes redirectAttributes) {
        try {
            Organization org = organizationService.findByUser(currentOrg);
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
                                @CurrentUser User currentOrg,
                                RedirectAttributes redirectAttributes) {
        try {
            Organization org = organizationService.findByUser(currentOrg);
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
                                  @CurrentUser User currentOrg,
                                  RedirectAttributes redirectAttributes) {
        try {
            Organization org = organizationService.findByUser(currentOrg);
            requestService.completeRequest(id, org);
            messages.flashSuccess(redirectAttributes, ServerMessage.FLASH_ORG_REQUEST_COMPLETED);
        } catch (IllegalStateException e) {
            messages.flashError(redirectAttributes, e);
        }
        return "redirect:" + Routes.ORG_REQUESTS;
    }

    /** Descarga un PDF con las solicitudes de la organización. */
    @PostMapping("/acopio/solicitudes/export-pdf")
    public void exportPdf(@CurrentUser User currentOrg,
                          HttpServletResponse response) {
        try {
            Organization org = organizationService.findByUser(currentOrg);
            List<Request> requests = requestService.getRequestsByOrganization(org, 0, 1000);
            byte[] pdfContent = pdfService.generateRequestsPdf(requests, org.getName());

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"solicitudes_acopio.pdf\"");
            response.setContentLength(pdfContent.length);
            response.getOutputStream().write(pdfContent);
            response.getOutputStream().flush();
        } catch (IOException e) {
            logger.error("Error generando PDF", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /** Toda operación de este controller que falle por no ser dueño de la solicitud cae acá. */
    @ExceptionHandler(SecurityException.class)
    public String handleNotOwned(RedirectAttributes redirectAttributes) {
        messages.flashError(redirectAttributes, ServerMessage.FLASH_ORG_REQUEST_NOT_OWNED);
        return "redirect:" + Routes.ORG_REQUESTS;
    }

    /** Descargar informe mensual en PDF. */
    @GetMapping("/acopio/reportes/mensual/descargar")
    public void downloadMonthlyReport(@RequestParam(required = false) String mes,
                                       @CurrentUser User currentOrg,
                                       HttpServletResponse response) {
        try {
            Organization org = organizationService.findByUser(currentOrg);
            YearMonth month = mes != null ? YearMonth.parse(mes) : YearMonth.now();

            var report = monthlyReportService.generateForMonth(org, month);
            byte[] pdfContent = pdfService.generateMonthlyReportPdf(report);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"informe_acopio_" + month + ".pdf\"");
            response.setContentLength(pdfContent.length);
            response.getOutputStream().write(pdfContent);
            response.getOutputStream().flush();

            logger.info("REPORT_DOWNLOADED: org={}, mes={}", org.getId(), month);
        } catch (IOException e) {
            logger.error("REPORT_DOWNLOAD_FAILED", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, List<Request>> groupByStatus(List<Request> requests) {
        Map<String, List<Request>> grouped = new LinkedHashMap<>();
        for (RequestStatus status : RequestStatus.values()) {
            grouped.put(status.name(), new ArrayList<>());
        }
        for (Request req : requests) {
            grouped.get(req.getStatus().name()).add(req);
        }
        return grouped;
    }
}
