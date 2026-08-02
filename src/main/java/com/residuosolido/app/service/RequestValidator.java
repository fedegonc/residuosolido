package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.User;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class RequestValidator {

    public void validateCreate(User user, City city, String address,
                                List<MaterialCategory> materials, String guestName, String guestPhone) {
        validateCoreFields(city, address, materials);
        if (user == null) {
            validateGuest(guestName, guestPhone);
        }
    }

    public void validateUpdate(City city, String address, List<MaterialCategory> materials) {
        validateCoreFields(city, address, materials);
    }

    private void validateCoreFields(City city, String address, List<MaterialCategory> materials) {
        if (city == null) {
            throw new IllegalArgumentException("error.request.city_required");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("error.request.address_required");
        }
        if (materials == null || materials.isEmpty()) {
            throw new IllegalArgumentException("error.request.materials_required");
        }
    }

    private void validateGuest(String guestName, String guestPhone) {
        if (guestName == null || guestName.trim().isEmpty()) {
            throw new IllegalArgumentException("error.request.guest_name_required");
        }
        PhoneNumber.of(guestPhone);
    }
}
