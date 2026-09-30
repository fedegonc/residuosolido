package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.Role;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Gestiona el perfil de negocio de las organizaciones (colección
 * {@code organizations}), separado del usuario de autenticación {@link User}.
 */
@Service
public class OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crea el perfil de negocio asociado a un usuario recién registrado como
     * organización. El id coincide con el del usuario para simplificar
     * referencias. Email y username se leen del User.
     */
    public Organization createForUser(User user) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        Organization org = new Organization();
        org.setId(user.getId());
        org.setPhone(user.getPhone());
        return organizationRepository.save(org);
    }

    public Organization findByUser(User user) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        return organizationRepository.findById(user.getId())
                .orElseThrow(() -> new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND));
    }

    public Organization findByUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        return organizationRepository.findById(userId)
                .orElseThrow(() -> new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND));
    }

    public Organization findById(String id) {
        if (id == null || id.isBlank()) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND));
    }

    /**
     * Actualiza el perfil de la organización. Email se actualiza en User (no aquí).
     * Evicta el cache de orgs por ciudad porque los materiales/ciudad cambian el resultado.
     */
    @CacheEvict(value = "orgsByCity", allEntries = true)
    public Organization updateProfile(User user, String name, String phone,
                                       City city, List<MaterialCategory> materials) {
        Organization org = findByUser(user);
        if (name != null) {
            org.setName(name.isBlank() ? null : name.trim());
        }
        if (phone != null) {
            org.setPhone(phone);
        }
        if (city != null) {
            org.setCity(city);
        }
        if (materials != null) {
            org.setAcceptedMaterials(materials);
        }
        if (org.hasPhone() && org.hasCity()) {
            org.completeProfile();
        }
        return organizationRepository.save(org);
    }

    @CacheEvict(value = "orgsByCity", allEntries = true)
    public void deleteWithUserSync(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        User user = userRepository.findById(organizationId).orElse(null);
        if (user != null && user.getRole() == Role.ORGANIZATION) {
            user.setRole(Role.USER);
            userRepository.save(user);
            logger.info("🔄 Organization deleted, User.role updated to USER: {}", organizationId);
        }
        organizationRepository.deleteById(organizationId);
        logger.info("✅ Organization deleted: {}", organizationId);
    }
}
