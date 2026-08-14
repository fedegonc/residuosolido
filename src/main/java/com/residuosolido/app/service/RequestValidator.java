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

    private void validateGuest(String guestName, String guestPhone) {
        Name.of(guestName);
        PhoneNumber.of(guestPhone);
    }
}
