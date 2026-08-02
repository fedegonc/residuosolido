package com.residuosolido.app.controller;

import com.residuosolido.app.dto.OrganizationDto;
import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.service.CityOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrgApiController {

    private final CityOrganizationService cityOrganizationService;

    @Autowired
    public OrgApiController(CityOrganizationService cityOrganizationService) {
        this.cityOrganizationService = cityOrganizationService;
    }

    @GetMapping("/api/organizations/by-city")
    public List<OrganizationDto> getOrganizationsByCity(@RequestParam City city) {
        return cityOrganizationService.getOrganizationsByCity(city).stream()
                .map(org -> new OrganizationDto(org.getId(), org.getDisplayName(), org.getAcceptedMaterials()))
                .toList();
    }
}
