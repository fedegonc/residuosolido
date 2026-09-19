package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import com.residuosolido.app.dto.OrganizationDto;
import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.service.CityOrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    /**
     * Devuelve las organizaciones de una ciudad como JSON.
     */
    @Operation(
            summary = "Listar organizaciones por ciudad",
            description = "Devuelve las organizaciones activas de una ciudad con los materiales que aceptan."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de organizaciones",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrganizationDto.class))
    )
    @GetMapping(Routes.API_ORGANIZATIONS_BY_CITY)
    public List<OrganizationDto> getOrganizationsByCity(
            @Parameter(description = "Ciudad para filtrar organizaciones", example = "RIVERA", required = true)
            @RequestParam("ciudad") City ciudad,
            @Parameter(description = "Material para filtrar organizaciones", example = "PLASTICO", required = false)
            @RequestParam(value = "material", required = false) MaterialCategory material) {
        return cityOrgService.getOrganizationsByCity(ciudad).stream()
                .filter(org -> material == null || org.getAcceptedMaterials().contains(material))
                .map(org -> new OrganizationDto(org.getId(), org.getDisplayName(), org.getAcceptedMaterials()))
                .toList();
    }
}
