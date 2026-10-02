package com.residuosolido.app.service;

import com.residuosolido.app.enums.OrgType;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.repository.OrganizationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Registro de organizaciones (empresas, municipios, acopios).
 * Implementa BaseRegistrationService<Organization> — hereda flujo común.
 * Entidad autónoma: username + PIN propios.
 */
@Service
public class OrganizationRegistrationService extends BaseRegistrationService<Organization> {

    private final OrganizationRepository organizationRepository;
    private OrgType registrationType;  // Capturado en validación

    public OrganizationRegistrationService(OrganizationRepository organizationRepository,
                                           PasswordEncoder passwordEncoder) {
        super(passwordEncoder);
        this.organizationRepository = organizationRepository;
    }

    /**
     * Registra organización con tipo.
     * Flujo: validate → prepare → insert
     */
    @Transactional
    public Organization register(Organization org, OrgType tipo) {
        if (tipo == null) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_ORG_TYPE_REQUIRED);
        }
        this.registrationType = tipo;
        return register(org);
    }

    // ===== Implementación de BaseRegistrationService<Organization> =====

    @Override
    protected ServerMessage validateEntity(Organization org) {
        if (org == null || org.getUsername() == null || org.getUsername().trim().isEmpty()) {
            return ServerMessage.ERROR_REGISTER_USERNAME_REQUIRED;
        }
        ServerMessage commonError = validateUsernameAndPin(org.getUsername(), org.getPassword());
        if (commonError != null) return commonError;

        if (org.getPhone() == null || org.getPhone().isBlank()) {
            return ServerMessage.ERROR_REGISTER_PHONE_REQUIRED;
        }
        if (registrationType == null) {
            return ServerMessage.ERROR_REGISTER_ORG_TYPE_REQUIRED;
        }
        return null;
    }

    @Override
    protected String getUsername(Organization org) {
        return org.getUsername();
    }

    @Override
    protected String getPassword(Organization org) {
        return org.getPassword();
    }

    @Override
    protected void setUsername(Organization org, String username) {
        org.setUsername(username);
    }

    @Override
    protected void setPassword(Organization org, String hashedPin) {
        org.setPassword(hashedPin);
    }

    @Override
    protected Optional<Organization> findByUsername(String username) {
        return organizationRepository.findByUsername(username);
    }

    @Override
    protected Organization insertEntity(Organization org) {
        org.setPhone(UserValidator.canonicalPhone(org.getPhone()));
        org.setTipo(registrationType);
        org.setActive(true);
        org.setCreatedAt(LocalDateTime.now());
        return organizationRepository.insert(org);
    }
}
