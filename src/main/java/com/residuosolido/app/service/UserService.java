package com.residuosolido.app.service;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestiona la identidad, autenticación y perfiles de usuario y organizaciones.
 * Provee resolución del usuario autenticado y actualización de datos de contacto.
 * Nota: MongoDB standalone no soporta transacciones multi-documento.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findAuthenticatedUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND));
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
    /**
     * allEntries=true en vez de evict por ciudad puntual: esta clase no sabe
     * (ni le corresponde saber) la ciudad ANTERIOR de la organización si el
     * update la cambia — evictar solo la ciudad nueva dejaría la vieja
     * cacheada con una organización que ya no debería aparecer ahí. El cache
     * tiene 2 entradas (RIVERA/LIVRAMENTO), evictarlo entero no tiene costo.
     */
    @CacheEvict(value = "orgsByCity", allEntries = true)
    public User updateUser(User user) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        User existing = userRepository.findById(user.getId())
                .orElseThrow(() -> new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND));

        if (user.getEmail() != null) {
            String normalized = user.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
            if (userRepository.findByEmailIgnoreCase(normalized).filter(other -> !other.getId().equals(existing.getId())).isPresent()) {
                throw new ValidationException(ServerMessage.ERROR_REGISTER_EMAIL_EXISTS);
            }
            existing.setEmail(normalized);
        }
        existing.setFirstName(user.getFirstName());
        existing.setPhone(user.getPhone());
        existing.setCity(user.getCity());
        existing.setAcceptedMaterials(user.getAcceptedMaterials());
        if (user.getProfileCompleted() != null) {
            existing.setProfileCompleted(user.getProfileCompleted());
        }

        try {
            return userRepository.save(existing);
        } catch (DuplicateKeyException e) {
            // El check de unicidad de email es check-then-act: si otro perfil
            // tomó el mismo email en la ventana, el índice único sparse decide.
            throw new ValidationException(ServerMessage.ERROR_REGISTER_EMAIL_EXISTS);
        }
    }

    public User updateProfile(User user, String email, String firstName, String phone, City city) {
        return updateProfile(user, email, firstName, phone, city, null);
    }

    public User updateProfile(User user, String email, String firstName, String phone, City city,
                               List<MaterialCategory> acceptedMaterials) {
        if (email != null) user.setEmail(email);
        if (firstName != null) user.setFirstName(firstName);
        if (phone != null) user.setPhone(phone);
        if (city != null) user.setCity(city);
        if (acceptedMaterials != null) user.setAcceptedMaterials(acceptedMaterials);
        if (user.isOrganization() && user.hasPhone() && user.hasCity()) {
            user.completeProfile();
        }
        return updateUser(user);
    }
}