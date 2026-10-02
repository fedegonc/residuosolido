package com.residuosolido.app.service;

import com.residuosolido.app.enums.OrgType;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Registro de ciudadanos (usuarios).
 * Implementa BaseRegistrationService<User> — hereda flujo común.
 */
@Service
public class UserRegistrationService extends BaseRegistrationService<User> {

    private final UserRepository userRepository;
    private final OrganizationService organizationService;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                     OrganizationService organizationService) {
        super(passwordEncoder);
        this.userRepository = userRepository;
        this.organizationService = organizationService;
    }

    // ===== Implementación de BaseRegistrationService<User> =====

    @Override
    protected ServerMessage validateEntity(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return ServerMessage.ERROR_REGISTER_USERNAME_REQUIRED;
        }
        ServerMessage commonError = validateUsernameAndPin(user.getUsername(), user.getPassword());
        if (commonError != null) return commonError;

        if (user.getPhone() == null || user.getPhone().isBlank()) {
            return ServerMessage.ERROR_REGISTER_PHONE_REQUIRED;
        }
        return null;
    }

    @Override
    protected String getUsername(User user) {
        return user.getUsername();
    }

    @Override
    protected String getPassword(User user) {
        return user.getPassword();
    }

    @Override
    protected void setUsername(User user, String username) {
        user.setUsername(username);
    }

    @Override
    protected void setPassword(User user, String hashedPin) {
        user.setPassword(hashedPin);
    }

    @Override
    protected Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    protected User insertEntity(User user) {
        user.setPhone(UserValidator.canonicalPhone(user.getPhone()));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.insert(user);
    }

    /**
     * Alias público para flujo de registro ciudadano.
     * Internamente delega a register() heredado de BaseRegistrationService.
     */
    public User registerCitizen(User user) {
        return register(user);
    }

    /**
     * DEPRECADO: Las organizaciones ahora se registran en OrganizationRegistrationService.
     * Este método queda solo para compatibilidad retroactiva con tests.
     * @deprecated Usar OrganizationRegistrationService.register() en su lugar
     */
    @Deprecated
    @Transactional
    public User registerOrganization(User user, OrgType tipo) {
        if (tipo == null) {
            throw new ValidationException(ServerMessage.ERROR_REGISTER_ORG_TYPE_REQUIRED);
        }
        User saved = register(user);
        organizationService.createForUser(saved, tipo);
        return saved;
    }

    /**
     * Valida un usuario para registro, devolviendo ServerMessage si hay error.
     * Método público para tests legacy que prueban validación isolada.
     * @deprecated Usar register(user) en producción; esta es solo para tests.
     * @return ServerMessage si hay error, null si es válido
     */
    @Deprecated
    public ServerMessage validateUserRegistration(User user) {
        return validateEntity(user);
    }
}
