package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrganizationResolver {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationResolver.class);

    private final CityOrganizationService cityOrganizationService;

    public OrganizationResolver(CityOrganizationService cityOrganizationService) {
        this.cityOrganizationService = cityOrganizationService;
    }

    public User resolve(String organizationId, City city) {
        if (organizationId != null && !organizationId.isBlank()) {
            return cityOrganizationService.findOrganizationByIdAndCity(organizationId, city);
        }
        return firstOrgForCity(city);
    }

    public User reassignIfNeeded(User currentOrg, City city) {
        if (currentOrg != null && currentOrg.getCity() != null && currentOrg.getCity().equals(city)) {
            return currentOrg;
        }
        return firstOrgForCity(city);
    }

    private User firstOrgForCity(City city) {
        List<User> orgs = cityOrganizationService.getOrganizationsByCity(city);
        if (orgs == null || orgs.isEmpty()) {
            throw new IllegalArgumentException("error.request.no_organization");
        }
        logger.info("Organización auto-asignada: {} para ciudad: {}", orgs.get(0).getDisplayName(), city);
        return orgs.get(0);
    }
}
