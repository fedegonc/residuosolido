package com.residuosolido.app.service;

import com.residuosolido.app.enums.OrgType;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Username;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationService organizationService;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                     OrganizationService organizationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationService = organizationService;
    }

    /**
     * Registro simplificado a propósito para facilitar las pruebas (ver
     * docs/TRADEOFFS.md §24): nombre en vez de usuario técnico (acepta espacios),
     * teléfono en vez de email, PIN de 4 dígitos en vez de contraseña.
     * Devuelve la clave del primer error encontrado, o null si es válido.
     */
    public ServerMessage validateUserRegistration(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return ServerMessage.ERROR_REGISTER_USERNAME_REQUIRED;
        }
        String canonical = Username.canonical(user.getUsername());
        if (canonical.length() > 64) return ServerMessage.ERROR_REGISTER_USERNAME_TOO_LONG;
        try {
            validatePin(user.getPassword());
        } catch (ValidationException e) {
            return e.key();
        }
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            return ServerMessage.ERROR_REGISTER_PHONE_REQUIRED;
        }
        if (userRepository.findByUsername(canonical).isPresent()) {
            return ServerMessage.ERROR_REGISTER_USERNAME_EXISTS;
        }
        return null;
    }

    /** Registro ciudadano: solo la cuenta — sin doc Organization ⇒ rol derivado USER. */
    public User registerCitizen(User user) {
        ServerMessage error = validateUserRegistration(user);
        if (error != null) throw new ValidationException(error);
        return insertAccount(user);
    }

    /** Registro organización: cuenta + doc Organization con tipo (esa existencia ES el rol). */
    public User registerOrganization(User user, OrgType tipo) {
        if (tipo == null) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_ORG_TYPE_REQUIRED);
        }
        ServerMessage error = validateUserRegistration(user);
        if (error != null) throw new ValidationException(error);
        User saved = insertAccount(user);
        organizationService.createForUser(saved, tipo);
        return saved;
    }

    private User insertAccount(User user) {
        User created = new User();
        created.setUsername(Username.canonical(user.getUsername()));
        created.setPhone(UserValidator.canonicalPhone(user.getPhone()));
        created.setPassword(passwordEncoder.encode(user.getPassword()));
        created.setActive(true);
        created.setCreatedAt(LocalDateTime.now());
        return userRepository.insert(created);
    }

    /** PIN de 4 dígitos — no es una contraseña real, es fricción mínima para pruebas. */
    private void validatePin(String value) {
        if (value == null || !value.matches("\\d{4}")) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_PIN_INVALID);
        }
    }
}
