package com.residuosolido.app.service;

import com.residuosolido.app.enums.Role;
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
     * docs/DEFENSA.md §24): nombre en vez de usuario técnico (acepta espacios),
     * teléfono en vez de email, PIN de 4 dígitos en vez de contraseña.
     */
    public String validateUserRegistration(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "error.register.username_required";
        }
        if (user.getUsername().length() > 64) return "error.register.username_too_long";
        try {
            validatePin(user.getPassword());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        if (user.getPhone() == null || user.getPhone().isBlank()) {
            return "error.register.phone_required";
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "error.register.username_exists";
        }
        return null;
    }

    public User registerUser(User user, String isOrganization) {
        boolean org = Boolean.parseBoolean(isOrganization);
        return registerUser(user, org);
    }

    public User registerUser(User user, boolean isOrganization) {
        String error = validateUserRegistration(user);
        if (error != null) throw new IllegalArgumentException(error);
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
            throw new IllegalArgumentException("error.register.pin_invalid");
        }
    }
}
