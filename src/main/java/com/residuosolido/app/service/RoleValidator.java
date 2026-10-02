package com.residuosolido.app.service;

import com.residuosolido.app.enums.Role;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import org.springframework.stereotype.Component;

/**
 * Revalidación de roles en requests críticos.
 *
 * Con in-memory sessions, el role se captura al login.
 * Si organización se elimina, sesión sigue siendo "válida" pero
 * el rol es incorrecto. Este helper revalida en endpoints críticos.
 *
 * Uso: en controladores que requieren ORGANIZATION role.
 */
@Component
public class RoleValidator {

    private final OrganizationRepository organizationRepository;

    public RoleValidator(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Revalida el rol derivado del usuario.
     * ¿Existe Organization con id == user.id?
     * → SÍ = ORGANIZATION, NO = CITIZEN
     */
    public Role getCurrentRole(User user) {
        if (user == null || user.getId() == null) {
            return Role.USER;
        }
        return organizationRepository.existsById(user.getId())
                ? Role.ORGANIZATION
                : Role.USER;
    }

    /**
     * Valida que usuario tiene rol ORGANIZATION.
     * Lanza ValidationException si no.
     */
    public void requireOrganization(User user) {
        if (getCurrentRole(user) != Role.ORGANIZATION) {
            throw new ValidationException(ServerMessage.FLASH_ERROR_ACCESS_DENIED);
        }
    }

    /**
     * Valida que usuario tiene rol USER (ciudadano).
     */
    public void requireCitizen(User user) {
        if (getCurrentRole(user) != Role.USER) {
            throw new ValidationException(ServerMessage.FLASH_ERROR_ACCESS_DENIED);
        }
    }
}
