package com.residuosolido.app.controller;

import com.residuosolido.app.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PublicMetricsController {

    private final RequestService requestService;

    @Autowired
    public PublicMetricsController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/metricas")
    public String publicMetrics(Model model) {
        model.addAttribute("metrics", requestService.getPublicMetricsByCity());
        model.addAttribute("total", requestService.getPublicTotalCompleted());
        return "metrics";
    }
}
