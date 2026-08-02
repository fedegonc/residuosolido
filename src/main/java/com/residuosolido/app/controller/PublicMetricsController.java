package com.residuosolido.app.controller;

import com.residuosolido.app.service.RequestMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicMetricsController {

    private final RequestMetricsService requestMetricsService;

    @Autowired
    public PublicMetricsController(RequestMetricsService requestMetricsService) {
        this.requestMetricsService = requestMetricsService;
    }

    @GetMapping("/metricas")
    public String publicMetrics(Model model) {
        model.addAttribute("metrics", requestMetricsService.getPublicMetricsByCity());
        model.addAttribute("total", requestMetricsService.getPublicTotalCompleted());
        return "public/metrics";
    }
}
