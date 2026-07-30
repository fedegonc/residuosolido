package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User findAuthenticatedUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("error.user.not_found"));
    }

    public boolean isAnonymous(Authentication authentication) {
        return authentication == null || "anonymousUser".equals(authentication.getPrincipal());
    }

    public User resolveUser(Authentication authentication) {
        if (isAnonymous(authentication)) {
            return null;
        }
        return findAuthenticatedUserByUsername(authentication.getName());
    }

    @Transactional
    public User updateUser(User user, String newPassword) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("error.user.not_found"));

        existing.setEmail(user.getEmail());
        existing.setFirstName(user.getFirstName());
        existing.setPhone(user.getPhone());
        existing.setCity(user.getCity());
        if (user.getProfileCompleted() != null) {
            existing.setProfileCompleted(user.getProfileCompleted());
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 8) {
                throw new IllegalArgumentException("error.register.password_min_length");
            }
            existing.setPassword(passwordEncoder.encode(newPassword));
        }

        return userRepository.save(existing);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User updateProfile(User user, String email, String firstName, String phone, City city) {
        if (email != null) user.setEmail(email.trim());
        if (firstName != null) user.setFirstName(firstName.trim());
        if (phone != null) user.setPhone(phone.trim());
        if (city != null) user.setCity(city);
        return updateUser(user, null);
    }

    @Transactional
    public void completeOrgProfile(User org, String phone, City city) {
        if (phone != null) org.setPhone(phone.trim());
        if (city != null) org.setCity(city);
        try {
            org.completeProfile();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        userRepository.save(org);
    }

    public String validateUserRegistration(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return "error.register.username_required";
        }
        if (user.getUsername().matches(".*\\s+.*")) {
            return "error.register.username_no_spaces";
        }
        if (user.getPassword() == null || user.getPassword().length() < 8) {
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