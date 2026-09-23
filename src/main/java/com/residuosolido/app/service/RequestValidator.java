package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.User;
import java.util.List;

/**
 * Validador de solicitudes: valida inputs de creación, edición, materiales y datos de huésped.
 * Responsabilidad única: todas las validaciones de negocio para Request.
 */
public class RequestValidator {

    public void validateCreate(User user, City city, String address, List<MaterialCategory> materials,
                               String guestName, String guestPhone, String organizationId) {
        validateCoreFields(city, address, materials, organizationId);
        if (user == null) {
            validateGuest(guestName, guestPhone);
        }
    }

    public void validateEstimates(String estimatedWeight, String estimatedVolume) {
        if ((estimatedWeight == null || estimatedWeight.trim().isEmpty()) &&
            (estimatedVolume == null || estimatedVolume.trim().isEmpty())) {
            throw new ValidationException(ServerMessage.ERROR_ESTIMATES_REQUIRED);
        }
    }

    public void validateMaterials(User org, List<MaterialCategory> materials) {
        if (materials == null || materials.isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_MATERIALS_REQUIRED);
        }
        if (org.getAcceptedMaterials() == null || org.getAcceptedMaterials().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_ORG_NO_MATERIALS);
        }
        for (MaterialCategory material : materials) {
            if (!org.getAcceptedMaterials().contains(material)) {
                throw new ValidationException(ServerMessage.ERROR_ORG_DOES_NOT_ACCEPT_MATERIAL);
            }
        }
    }

    private void validateCoreFields(City city, String address, List<MaterialCategory> materials, String organizationId) {
        if (city == null) {
            throw new ValidationException(ServerMessage.ERROR_CITY_REQUIRED);
        }
        if (address == null || address.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_ADDRESS_REQUIRED);
        }
        if (address.length() > 256) {
            throw new ValidationException(ServerMessage.ERROR_ADDRESS_TOO_LONG);
        }
        if (materials == null || materials.isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_MATERIALS_REQUIRED);
        }
        if (organizationId == null || organizationId.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_ORG_REQUIRED);
        }
    }

    private void validateGuest(String guestName, String guestPhone) {
        if (guestName == null || guestName.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_GUEST_NAME_REQUIRED);
        }
        if (guestName.length() > 128) {
            throw new ValidationException(ServerMessage.ERROR_GUEST_NAME_TOO_LONG);
        }
        if (guestPhone == null || guestPhone.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
        PhoneNumber phone = new PhoneNumber(guestPhone);
        phone.validate();
    }
}
