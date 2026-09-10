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
            AccountInput.password(user.getPassword());
            AccountInput.email(user.getEmail());
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "error.register.username_exists";
        }
        if (userRepository.findByEmailIgnoreCase(AccountInput.email(user.getEmail())).isPresent()) {
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
        created.setEmail(AccountInput.email(user.getEmail()));
        created.setPassword(passwordEncoder.encode(user.getPassword()));
        created.setRole(isOrganization ? Role.ORGANIZATION : Role.USER);
        created.setActive(true);
        created.setCreatedAt(LocalDateTime.now());
        return userRepository.insert(created);
    }
}
