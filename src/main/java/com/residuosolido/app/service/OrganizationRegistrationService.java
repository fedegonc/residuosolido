package com.residuosolido.app.service;

import com.residuosolido.app.enums.OrgType;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.Username;
import com.residuosolido.app.repository.OrganizationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Registro de organizaciones (empresas, municipios, acopios).
 * Entidad autónoma: username + PIN propios.
 */
@Service
public class OrganizationRegistrationService {

    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public OrganizationRegistrationService(OrganizationRepository organizationRepository,
                                           PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ServerMessage validateRegistration(Organization org, OrgType tipo) {
        if (org == null || org.getUsername() == null || org.getUsername().trim().isEmpty()) {
            return ServerMessage.ERROR_REGISTER_USERNAME_REQUIRED;
        }
        String canonical = Username.canonical(org.getUsername());
        if (canonical.length() > 64) return ServerMessage.ERROR_REGISTER_USERNAME_TOO_LONG;
        try {
            validatePin(org.getPassword());
        } catch (ValidationException e) {
            return e.key();
        }
        if (org.getPhone() == null || org.getPhone().isBlank()) {
            return ServerMessage.ERROR_REGISTER_PHONE_REQUIRED;
        }
        if (tipo == null) {
            return ServerMessage.ERROR_REGISTER_ORG_TYPE_REQUIRED;
        }
        if (organizationRepository.findByUsername(canonical).isPresent()) {
            return ServerMessage.ERROR_REGISTER_USERNAME_EXISTS;
        }
        return null;
    }

    /**
     * Registra organización: crea document en organizations con username/PIN.
     * Transaccional: atomicidad garantizada.
     */
    @Transactional
    public Organization register(Organization org, OrgType tipo) {
        ServerMessage error = validateRegistration(org, tipo);
        if (error != null) throw new ValidationException(error);

        Organization created = new Organization();
        created.setUsername(Username.canonical(org.getUsername()));
        created.setPassword(passwordEncoder.encode(org.getPassword()));
        created.setPhone(UserValidator.canonicalPhone(org.getPhone()));
        created.setTipo(tipo);
        created.setActive(true);
        created.setCreatedAt(LocalDateTime.now());

        return organizationRepository.insert(created);
    }

    private void validatePin(String value) {
        if (value == null || !value.matches("\\d{4}")) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_PIN_INVALID);
        }
    }
}
