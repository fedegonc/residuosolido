package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.Name;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.User;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class RequestValidator {

    public void validateCreate(User user, City city, String address,
                                List<MaterialCategory> materials, String guestName, String guestPhone,
                                String organizationId) {
        validateCoreFields(city, address, materials, organizationId);
        if (user == null) {
            validateGuest(guestName, guestPhone);
        } else {
            if (!user.isActive() || user.getRole() != com.residuosolido.app.enums.Role.USER) {
                throw new IllegalArgumentException("error.request.citizen_required");
            }
            if (!PhoneNumber.isValid(user.getPhone())) {
                throw new IllegalArgumentException("error.profile.phone_required");
            }
        }
    }

    public void validateUpdate(City city, String address, List<MaterialCategory> materials, String organizationId) {
        validateCoreFields(city, address, materials, organizationId);
    }

    private void validateCoreFields(City city, String address, List<MaterialCategory> materials, String organizationId) {
        if (city == null) {
            throw new IllegalArgumentException("error.request.city_required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("error.request.address_required");
        }
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("error.request.materials_required");
        }
        if (organizationId == null || organizationId.isBlank()) {
            throw new IllegalArgumentException("error.request.organization_required");
        }
    }

    public void validateMaterials(User organization, List<MaterialCategory> materials) {
        if (organization.getAcceptedMaterials() == null || materials == null || materials.isEmpty()
                || materials.stream().anyMatch(m -> m == null || !organization.getAcceptedMaterials().contains(m))) {
            throw new IllegalArgumentException("error.request.materials_not_accepted");
        }
    }

    public void validateEstimates(String weight, String volume) {
        if (weight != null && !weight.isBlank() && !List.of("0-5", "5-20", "20-50", "50+").contains(weight)) {
            throw new IllegalArgumentException("error.request.invalid_weight");
        }
        if (volume != null && !volume.isBlank() && !List.of("bag", "box", "trunk", "pickup").contains(volume)) {
            throw new IllegalArgumentException("error.request.invalid_volume");
        }
    }

    private void validateGuest(String guestName, String guestPhone) {
        Name.of(guestName);
        PhoneNumber.of(guestPhone);
    }
}
