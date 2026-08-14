package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.User;
import org.springframework.stereotype.Component;

/**
 * Resuelve la organización de una solicitud a partir de la elección explícita
 * del usuario. No existe asignación automática por proximidad ni por "primera
 * organización disponible": si no se eligió organización, se rechaza (ver
 * RequestValidator).
 */
@Component
public class OrgResolver {

    private final CityOrgService cityOrgService;

    public OrgResolver(CityOrgService cityOrgService) {
        this.cityOrgService = cityOrgService;
    }

    public User resolve(String organizationId, City city) {
        return cityOrgService.findOrganizationByIdAndCity(organizationId, city);
    }
}
