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

    public String validateUserRegistration(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "error.register.username_required";
        }
        if (user.getUsername().matches(".*\\s+.*")) {
            return "error.register.username_no_spaces";
        }
        if (user.getUsername().length() > 64) return "error.register.username_too_long";
        try {
            validatePassword(user.getPassword());
            validateEmail(user.getEmail());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "error.register.username_exists";
        }
        if (userRepository.findByEmailIgnoreCase(user.getEmail().trim().toLowerCase(java.util.Locale.ROOT)).isPresent()) {
            return "error.register.email_exists";
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
        created.setEmail(user.getEmail());
        created.setPassword(passwordEncoder.encode(user.getPassword()));
        created.setRole(isOrganization ? Role.ORGANIZATION : Role.USER);
        created.setActive(true);
        created.setCreatedAt(LocalDateTime.now());
        return userRepository.insert(created);
    }

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("error.register.email_invalid");
        }
        String normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("error.register.email_invalid");
        }
    }

    private void validatePassword(String value) {
        if (value == null || value.isBlank() || value.length() < 8) {
            throw new IllegalArgumentException("error.register.password_min_length");
        }
        if (value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("error.register.password_too_long");
        }
    }
}
