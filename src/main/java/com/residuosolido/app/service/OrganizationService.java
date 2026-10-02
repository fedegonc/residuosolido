package com.residuosolido.app.service;

import com.residuosolido.app.enums.City;
import com.residuosolido.app.enums.MaterialCategory;
import com.residuosolido.app.enums.OrgType;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * Crea el perfil de negocio asociado a un usuario recién registrado como
     * organización. El id coincide con el del usuario para simplificar
     * referencias. Email y username se leen del User.
     */
    public Organization createForUser(User user, OrgType tipo) {
        if (user == null) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        if (tipo == null) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_ORG_TYPE_REQUIRED);
        }
        Organization org = new Organization();
        org.setId(user.getId());
        org.setTipo(tipo);
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
     * Si la cuenta es organización: existe doc en {@code organizations} con su
     * _id. Única fuente de verdad del rol — User no guarda campo role.
     */
    public boolean isOrganization(User user) {
        return user != null && user.getId() != null
                && organizationRepository.existsById(user.getId());
    }

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

    public void delete(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) {
            throw new ValidationException(ServerMessage.ERROR_USER_NOT_FOUND);
        }
        organizationRepository.deleteById(organizationId);
        // No hay User.role que sincronizar: borrar el doc ES degradar a ciudadano.
        logger.info("✅ Organization deleted: {}", organizationId);
    }
}
