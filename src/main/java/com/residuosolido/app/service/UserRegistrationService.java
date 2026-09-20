package com.residuosolido.app.service;

import com.residuosolido.app.enums.Role;
import com.residuosolido.app.enums.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        if (user.getUsername().length() > 64) return ServerMessage.ERROR_REGISTER_USERNAME_TOO_LONG;
        try {
            validatePin(user.getPassword());
        } catch (ValidationException e) {
            return e.key();
        }
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            return ServerMessage.ERROR_REGISTER_PHONE_REQUIRED;
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ServerMessage.ERROR_REGISTER_USERNAME_EXISTS;
        }
        return null;
    }

    public User registerUser(User user, String isOrganization) {
        boolean org = Boolean.parseBoolean(isOrganization);
        return registerUser(user, org);
    }

    public User registerUser(User user, boolean isOrganization) {
        ServerMessage error = validateUserRegistration(user);
        if (error != null) throw new ValidationException(error);
        User created = new User();
        created.setUsername(user.getUsername());
        created.setPhone(user.getPhone());
        created.setPassword(passwordEncoder.encode(user.getPassword()));
        created.setRole(isOrganization ? Role.ORGANIZATION : Role.USER);
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
