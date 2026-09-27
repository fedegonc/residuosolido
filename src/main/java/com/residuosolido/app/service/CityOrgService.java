package com.residuosolido.app.service;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CityOrgService {

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
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_NOT_FOUND));
        if (org.getCity() == null || org.getCity() != city) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_NOT_IN_CITY);
        }
        if (!isAvailable(org)) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_UNAVAILABLE);
        }
        return org;
    }

    /**
     * Cacheado: se llama en cada carga del formulario de solicitud (guest y
     * usuario) y cambia solo cuando una organización actualiza su perfil
     * (evict explícito en OrganizationService.updateProfile).
     */
    @Cacheable(value = "orgsByCity", key = "#city")
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
