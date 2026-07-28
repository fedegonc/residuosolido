package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.service.RequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrgApiController {

    private final RequestService requestService;

    @Autowired
    public OrgApiController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/api/organizations/by-city")
    public List<User> getOrganizationsByCity(@RequestParam City city) {
        return requestService.getOrganizationsByCity(city);
    }
}
