package com.residuosolido.app.service;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CityOrgService {

    private final UserRepository userRepository;

    @Autowired
    public CityOrgService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Resuelve la organización de una solicitud a partir de la elección explícita
     * del usuario. No existe asignación automática por proximidad ni por "primera
     * organización disponible": si no se eligió organización, se rechaza desde
     * RequestService.validateCoreFields.
     */
    public User findOrganizationByIdAndCity(String organizationId, City city) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
        if (city == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_CITY_REQUIRED);
        }
        User org = userRepository.findById(organizationId)
                .orElseThrow(() -> new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_NOT_FOUND));
        if (!org.isOrganization()) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ASSIGN_NOT_ORGANIZATION);
        }
        if (org.getCity() == null || org.getCity() != city) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_NOT_IN_CITY);
        }
        if (!isAvailable(org)) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_UNAVAILABLE);
        }
        return org;
    }

    public List<User> getOrganizationsByCity(City city) {
        return userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, city, true)
                .stream().filter(this::isAvailable).toList();
    }

    private boolean isAvailable(User org) {
        return org.isActive() && org.isOrganization() && org.isProfileComplete()
                && com.residuosolido.app.model.PhoneNumber.isValid(org.getPhone())
                && org.getAcceptedMaterials() != null && !org.getAcceptedMaterials().isEmpty();
    }

    public List<City> getAvailableCities() {
        return Arrays.asList(City.values());
    }
}
