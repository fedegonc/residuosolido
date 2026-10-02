package com.residuosolido.app.service;

import com.residuosolido.app.exception.OwnershipException;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import org.springframework.stereotype.Component;

/**
 * Autorización integrada: rol + propiedad en un único lugar.
 *
 * Patrón: autorización en capas
 * 1. Rol: @PreAuthorize en controlador (qué tipo de usuario)
 * 2. Propiedad: AuthorizationService (qué recurso específico)
 *
 * Este servicio COMBINA ambos chequeos para evitar duplicación
 * y mantener coherencia entre reglas de rol y propiedad.
 */
@Component
public class AuthorizationService {

    private final RoleValidator roleValidator;
    private final RequestValidator requestValidator;

    public AuthorizationService(RoleValidator roleValidator, RequestValidator requestValidator) {
        this.roleValidator = roleValidator;
        this.requestValidator = requestValidator;
    }

    // ===== CITIZEN (Usuario ciudadano) =====

    /**
     * Valida que usuario es ciudadano Y que la solicitud le pertenece.
     * Falla en dos puntos: si no es ciudadano O si no es su solicitud.
     *
     * Uso: en endpoints ciudadano que requieren acceso a solicitud específica.
     */
    public void requireCitizenOwnerOfRequest(User user, Request request) {
        roleValidator.requireCitizen(user);
        requestValidator.requireOwnedByCitizen(request, user);
    }

    /**
     * Solo valida propiedad: ¿la solicitud le pertenece al usuario?
     * Usado cuando ya verificamos rol en @PreAuthorize.
     */
    public void requireRequestOwner(Request request, User user) {
        requestValidator.requireOwnedByCitizen(request, user);
    }

    // ===== ORGANIZATION (Organización) =====

    /**
     * Valida que usuario es organización Y que la solicitud le está asignada.
     * Falla en dos puntos: si no es org O si no es su solicitud.
     *
     * Uso: en endpoints org que requieren acceso a solicitud específica.
     */
    public void requireOrganizationOwnerOfRequest(User user, Request request) {
        roleValidator.requireOrganization(user);
        requestValidator.requireOwnedByOrganization(request,
            getOrganizationForUser(user));
    }

    /**
     * Solo valida propiedad: ¿la solicitud le pertenece a la organización?
     * Usado cuando ya verificamos rol en @PreAuthorize.
     */
    public void requireOrganizationOwnsRequest(Request request, Organization org) {
        requestValidator.requireOwnedByOrganization(request, org);
    }

    // ===== HELPER: obtener Organization de User actual =====

    /**
     * TODO (cuando separemos User y Organization en auth)
     * Por ahora, retorna la primera que encuentra o lanza error.
     */
    private Organization getOrganizationForUser(User user) {
        // Placeholder: cuando refactoricemos SecurityBeansConfig,
        // buscaremos Organization por userId o similar
        throw new ValidationException(ServerMessage.FLASH_ERROR_ACCESS_DENIED);
    }
}
