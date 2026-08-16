package com.residuosolido.app.service;

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
     * organización disponible": si no se eligió organización, se rechaza (ver
     * RequestValidator).
     */
    public User findOrganizationByIdAndCity(String organizationId, City city) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("error.request.organization_required");
        }
        if (city == null) {
            throw new IllegalArgumentException("error.request.city_required");
        }
        User org = userRepository.findById(organizationId)
                .orElseThrow(() -> new IllegalArgumentException("error.request.organization_not_found"));
        if (!org.isOrganization()) {
            throw new IllegalArgumentException("error.request.assign_not_organization");
        }
        if (org.getCity() == null || org.getCity() != city) {
            throw new IllegalArgumentException("error.request.organization_not_in_city");
        }
        return org;
    }

    public List<User> getOrganizationsByCity(City city) {
        List<User> orgs = userRepository.findByRoleAndCityAndActive(Role.ORGANIZATION, city, true);
        if (orgs == null || orgs.isEmpty()) {
            orgs = userRepository.findByRoleAndCity(Role.ORGANIZATION, city);
        }
        return orgs;
    }

    public List<City> getAvailableCities() {
        return Arrays.stream(City.values())
                .filter(c -> {
                    List<User> orgs = getOrganizationsByCity(c);
                    return orgs != null && !orgs.isEmpty();
                })
                .toList();
    }
}
