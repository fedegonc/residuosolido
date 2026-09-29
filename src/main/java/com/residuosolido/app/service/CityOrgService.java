package com.residuosolido.app.service;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CityOrgService {

    private static final Logger logger = LoggerFactory.getLogger(CityOrgService.class);
    private final OrganizationRepository organizationRepository;

    @Autowired
    public CityOrgService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Resuelve la organización de una solicitud a partir de la elección explícita
     * del usuario. No existe asignación automática por proximidad ni por "primera
     * organización disponible": si no se eligió organización, se rechaza desde
     * RequestService.validateCoreFields.
     */
    public Organization findOrganizationByIdAndCity(String organizationId, City city) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
        if (city == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_CITY_REQUIRED);
        }
        logger.info("findOrganizationByIdAndCity: id={}, city={}", organizationId, city);
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> {
                    logger.warn("Organización no encontrada por ID: {}", organizationId);
                    return new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_NOT_FOUND);
                });
        logger.info("Organización encontrada: id={}, org.city={}", organizationId, org.getCity());
        if (org.getCity() == null || !org.getCity().equals(city)) {
            logger.warn("City mismatch: org.city={}, required={}", org.getCity(), city);
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_NOT_IN_CITY);
        }
        if (!isAvailable(org)) {
            logger.warn("Organización no disponible: id={}, profile={}, phone={}, materials={}",
                    organizationId, org.isProfileComplete(), org.getPhone(), org.getAcceptedMaterials());
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_UNAVAILABLE);
        }
        return org;
    }

    public List<Organization> getOrganizationsByCity(City city) {
        if (city == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_CITY_REQUIRED);
        }
        return organizationRepository.findByCity(city)
                .stream().filter(this::isAvailable).toList();
    }

    private boolean isAvailable(Organization org) {
        return org.isProfileComplete()
                && com.residuosolido.app.model.PhoneNumber.isValid(org.getPhone())
                && org.getAcceptedMaterials() != null && !org.getAcceptedMaterials().isEmpty();
    }

    public List<City> getAvailableCities() {
        return Arrays.asList(City.values());
    }
}
