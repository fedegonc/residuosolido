package com.residuosolido.app.controller;

import com.residuosolido.app.model.Request;
import com.residuosolido.app.service.GenericEntityService;
import com.residuosolido.app.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/requests")
public class RequestController extends GenericEntityController<Request, Long> {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @Override
    protected GenericEntityService<Request, Long> getService() {
        return requestService;
    }

    @Override
    protected String getEntityName() {
        return "request";
    }

    @Override
    protected String getEntityNamePlural() {
        return "requests";
    }

    @Override
    protected String getBasePath() {
        return "/requests";
    }

    @Override
    protected Request createNewEntity() {
        // Crear nueva instancia usando constructor
        return new Request();
    }

    @Override
    protected Long getEntityId(Request entity) {
        return entity.getId();
    }

    @GetMapping
    public String listRequests(Model model) {
        model.addAttribute("requests", requestService.findAll());
        return "requests/list";
    }
}
