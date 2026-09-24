package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validaciones centralizadas para Request.
 * Contrato: métodos puros (sin lado-efectos), lanzan excepción o devuelven silenciosamente.
 */
@Component
public class RequestValidator {

    /**
     * Valida los campos obligatorios para crear una solicitud (ciudadano o invitado).
     * Ciudadano: user no nulo, activo, con rol USER y teléfono válido.
     * Invitado: name y phone válidos.
     */
    public void validateCreate(User user, City city, String address,
                               List<MaterialCategory> materials, String guestName, String guestPhone,
                               String organizationId) {
        validateCoreFields(city, address, materials, organizationId);
        if (user == null) {
            validateGuest(guestName, guestPhone);
        } else {
            if (!user.isActive() || user.getRole() != Role.USER) {
                throw new ValidationException(ServerMessage.ERROR_REQUEST_CITIZEN_REQUIRED);
            }
            if (!PhoneNumber.isValid(user.getPhone())) {
                throw new ValidationException(ServerMessage.ERROR_PROFILE_PHONE_REQUIRED);
            }
        }
    }

    /**
     * Valida los campos editables de una solicitud (ciudad, dirección, materiales, org).
     */
    public void validateUpdate(City city, String address, List<MaterialCategory> materials, String organizationId) {
        validateCoreFields(city, address, materials, organizationId);
    }

    /**
     * Valida que los materiales seleccionados sean aceptados por la organización.
     */
    public void validateMaterials(User organization, List<MaterialCategory> materials) {
        if (organization.getAcceptedMaterials() == null || materials == null || materials.isEmpty()
                || materials.stream().anyMatch(m -> m == null || !organization.getAcceptedMaterials().contains(m))) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_MATERIALS_NOT_ACCEPTED);
        }
    }

    // ========== Private ==========

    private void validateCoreFields(City city, String address, List<MaterialCategory> materials, String organizationId) {
        if (city == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_CITY_REQUIRED);
        }
        if (address == null || address.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ADDRESS_REQUIRED);
        }
        if (materials == null || materials.isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_MATERIALS_REQUIRED);
        }
        if (organizationId == null || organizationId.isBlank()) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
    }

    private void validateGuest(String guestName, String guestPhone) {
        if (guestName == null || guestName.trim().isEmpty()) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_GUEST_NAME_REQUIRED);
        }
        if (guestName.trim().length() > 100) {
            throw new ValidationException(ServerMessage.ERROR_NAME_TOO_LONG);
        }
        PhoneNumber.normalize(guestPhone);
    }
}
