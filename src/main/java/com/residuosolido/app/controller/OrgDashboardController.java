package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.service.BreadcrumbService;
import com.residuosolido.app.service.RequestMetricsService;
import com.residuosolido.app.service.RequestOrgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

/** Dashboard de organización: muestra estadísticas y solicitudes pendientes recientes. */
@Controller
@PreAuthorize("hasRole('ORGANIZATION')")
public class OrgDashboardController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrgDashboardController.class);

    private final RequestMetricsService requestMetricsService;
    private final RequestOrgService requestOrgService;
    private final BreadcrumbService breadcrumbService;

    @Autowired
    public OrgDashboardController(RequestMetricsService requestMetricsService,
                                   RequestOrgService requestOrgService,
                                   BreadcrumbService breadcrumbService) {
        this.requestMetricsService = requestMetricsService;
        this.requestOrgService = requestOrgService;
        this.breadcrumbService = breadcrumbService;
    }

    /** Página principal de la organización. Redirige a completar perfil si falta. */
    @GetMapping("/acopio/inicio")
    public String orgDashboard(Authentication authentication, Model model) {
        User currentOrg = getCurrentUser(authentication);

        if (currentOrg.needsProfileCompletion()) {
            return "redirect:/acopio/completar-perfil";
        }

        model.addAttribute("user", currentOrg);
        try {
            Map<String, Long> data = requestMetricsService.getOrgDashboardData(currentOrg);
            model.addAttribute("pendingRequests", data.get("pending"));
            model.addAttribute("inProgressRequests", data.get("inProgress"));
            model.addAttribute("completedRequests", data.get("completed"));
            model.addAttribute("pendingRequestsList", requestOrgService.getRecentPendingRequestsByOrganization(currentOrg, 5));
        } catch (Exception e) {
            logger.error("Error en dashboard de organización: {}", e.getMessage(), e);
            model.addAttribute("pendingRequests", 0);
            model.addAttribute("inProgressRequests", 0);
            model.addAttribute("completedRequests", 0);
            model.addAttribute("pendingRequestsList", List.of());
        }
        model.addAttribute("breadcrumbs", breadcrumbService.addCurrent(breadcrumbService.home(), "Panel de acopio"));
        return "org/dashboard";
    }
}
