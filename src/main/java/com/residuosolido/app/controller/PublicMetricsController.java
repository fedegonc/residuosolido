package com.residuosolido.app.controller;

import com.residuosolido.app.service.PublicMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicMetricsController {

    private final PublicMetricsService publicMetricsService;

    @Autowired
    public PublicMetricsController(PublicMetricsService publicMetricsService) {
        this.publicMetricsService = publicMetricsService;
    }

    @GetMapping("/metricas")
    public String publicMetrics(Model model) {
        model.addAttribute("metrics", publicMetricsService.getPublicMetricsByCity());
        model.addAttribute("total", publicMetricsService.getPublicTotalCompleted());
        return "public/metrics";
    }
}
