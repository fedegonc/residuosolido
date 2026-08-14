package com.residuosolido.app.controller;

import com.residuosolido.app.dto.OrganizationDto;
import com.residuosolido.app.model.User;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.service.CityOrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API REST que devuelve organizaciones disponibles por ciudad (para filtrado dinámico en formularios). */
@RestController
public class OrgApiController {

    private final CityOrgService cityOrgService;

    @Autowired
    public OrgApiController(CityOrgService cityOrgService) {
        this.cityOrgService = cityOrgService;
    }

    /** Devuelve las organizaciones de una ciudad como JSON (id, nombre, materiales aceptados). */
    @GetMapping("/api/organizations/by-city")
    public List<OrganizationDto> getOrganizationsByCity(@RequestParam City city) {
        return cityOrgService.getOrganizationsByCity(city).stream()
                .map(org -> new OrganizationDto(org.getId(), org.getDisplayName(), org.getAcceptedMaterials()))
                .toList();
    }
}
