package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.exception.OwnershipException;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validaciones centralizadas para Request.
 * Contrato: métodos puros (sin lado-efectos), lanzan excepción o devuelven silenciosamente.
 */
@Component
public class RequestValidator {

    private final OrganizationRepository organizationRepository;

    public RequestValidator(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Valida los campos obligatorios para crear una solicitud.
     * Ciudadano: user no nulo, activo, sin doc Organization y teléfono válido.
     */
    public void validateCreate(User user, City city, String address,
                               List<MaterialCategory> materials, String organizationId) {
        validateCoreFields(city, address, materials, organizationId);
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_CITIZEN_REQUIRED);
        }
        // Ciudadano = cuenta sin Organization asociada (rol derivado, no campo).
        if (!user.isActive() || (user.getId() != null && organizationRepository.existsById(user.getId()))) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_CITIZEN_REQUIRED);
        }
        if (!PhoneNumber.isValid(user.getPhone())) {
            throw new ValidationException(ServerMessage.ERROR_PROFILE_PHONE_REQUIRED);
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
    public void validateMaterials(Organization organization, List<MaterialCategory> materials) {
        if (organization == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
        if (organization.getAcceptedMaterials() == null || materials == null || materials.isEmpty()
                || materials.stream().anyMatch(m -> m == null || !organization.getAcceptedMaterials().contains(m))) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_MATERIALS_NOT_ACCEPTED);
        }
    }

    /** Lanza si el ciudadano es null — clave de creación de solicitud. */
    public void requireCitizen(User user) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_CITIZEN_REQUIRED);
        }
    }

    /** Lanza si el usuario es null — clave de "usuario no encontrado". */
    public void requireUser(User user) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
    }

    /** Lanza si la organización es null. */
    public void requireOrganization(Organization organization) {
        if (organization == null) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
    }

    /** Lanza OwnershipException si la solicitud no pertenece al ciudadano. */
    public void requireOwnedByCitizen(Request request, User user) {
        if (request == null || user == null || request.getUser() == null
                || !request.getUser().getId().equals(user.getId())) {
            throw new OwnershipException(ServerMessage.FLASH_REQUEST_NOT_OWNED);
        }
    }

    /** Lanza OwnershipException si la solicitud no está asignada a la organización. */
    public void requireOwnedByOrganization(Request request, Organization organization) {
        if (request == null || organization == null || request.getOrganization() == null
                || !request.getOrganization().getId().equals(organization.getId())) {
            throw new OwnershipException(ServerMessage.FLASH_ORG_REQUEST_NOT_OWNED);
        }
    }

    // ========== Private ==========

    private void validateCoreFields(City city, String address, List<MaterialCategory> materials, String organizationId) {
        Request.validateDraft(city, address, materials);
        if (organizationId == null || organizationId.isBlank()) {
            throw new ValidationException(ServerMessage.ERROR_REQUEST_ORGANIZATION_REQUIRED);
        }
    }
}
