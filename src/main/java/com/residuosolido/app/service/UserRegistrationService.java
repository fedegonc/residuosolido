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
        if (user.getPassword() == null || user.getPassword().length() < 3) {
            return "error.register.password_min_length";
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty() || !user.getEmail().contains("@")) {
            return "error.register.email_invalid";
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "error.register.username_exists";
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "error.register.email_exists";
        }
        return null;
    }

    public User registerUser(User user, String isOrganization) {
        boolean org = isOrganization != null;
        return registerUser(user, org);
    }

    public User registerUser(User user, boolean isOrganization) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(isOrganization ? Role.ORGANIZATION : Role.USER);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
