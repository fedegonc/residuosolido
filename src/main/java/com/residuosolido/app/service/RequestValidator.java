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
        if (city == null) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED); // placeholder
        }
        if (address == null || address.trim().isEmpty() || address.length() > 256) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
        if (materials == null || materials.isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
        if (organizationId == null || organizationId.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
        if (user == null) {
            validateGuest(guestName, guestPhone);
        }
    }

    public void validateEstimates(String estimatedWeight, String estimatedVolume) {
        if ((estimatedWeight == null || estimatedWeight.trim().isEmpty()) &&
            (estimatedVolume == null || estimatedVolume.trim().isEmpty())) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
    }

    public void validateMaterials(User org, List<MaterialCategory> materials) {
        if (materials == null || materials.isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
        if (org.getAcceptedMaterials() == null || org.getAcceptedMaterials().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
        for (MaterialCategory material : materials) {
            if (!org.getAcceptedMaterials().contains(material)) {
                throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
            }
        }
    }

    private void validateGuest(String guestName, String guestPhone) {
        if (guestName == null || guestName.trim().isEmpty() || guestName.length() > 128) {
            throw new ValidationException(ServerMessage.ERROR_NAME_REQUIRED);
        }
        if (guestPhone == null || guestPhone.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_PHONE_REQUIRED);
        }
        PhoneNumber.normalize(guestPhone);
    }
}
