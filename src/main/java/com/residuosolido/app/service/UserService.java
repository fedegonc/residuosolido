package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.PhoneNumber;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    // NOTE: MongoDB standalone does not support multi-document transactions (requires replica set).
    // These operations are NOT atomic. If a failure occurs mid-operation, data may be left inconsistent.
    // To enable real transactions, configure a single-node replica set in MongoDB.
    public User updateUser(User user, String newPassword) {
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("error.user.not_found"));

        existing.setEmail(user.getEmail());
        existing.setFirstName(user.getFirstName());
        existing.setPhone(user.getPhone());
        existing.setCity(user.getCity());
        existing.setAcceptedMaterials(user.getAcceptedMaterials());
        if (user.getProfileCompleted() != null) {
            existing.setProfileCompleted(user.getProfileCompleted());
        }

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 3) {
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
        return updateProfile(user, email, firstName, phone, city, null);
    }

    public User updateProfile(User user, String email, String firstName, String phone, City city,
                               List<MaterialCategory> acceptedMaterials) {
        if (email != null) user.setEmail(email.trim());
        if (firstName != null) user.setFirstName(firstName.trim());
        if (phone != null) {
            PhoneNumber.of(phone);
            user.setPhone(phone.trim());
        }
        if (city != null) user.setCity(city);
        if (acceptedMaterials != null) user.setAcceptedMaterials(acceptedMaterials);
        return updateUser(user, null);
    }

    // NOTE: Not @Transactional — MongoDB standalone has no transaction support.
    public void completeOrgProfile(User org, String phone, City city) {
        if (phone != null) {
            PhoneNumber.of(phone);
            org.setPhone(phone.trim());
        }
        if (city != null) org.setCity(city);
        try {
            org.completeProfile();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        userRepository.save(org);
    }
}