package com.residuosolido.app.config;

import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrganizationIntegrityValidator implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationIntegrityValidator.class);
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public OrganizationIntegrityValidator(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("🔍 Validando integridad: User.role ↔ Organization");

        // Invariante 1: Todo User.role=ORGANIZATION debe tener Organization record
        List<User> orgUsers = userRepository.findByRole(Role.ORGANIZATION);
        for (User user : orgUsers) {
            if (!organizationRepository.existsById(user.getId())) {
                String msg = String.format(
                    "INTEGRITY VIOLATION: User '%s' (id=%s) tiene role=ORGANIZATION pero NO existe Organization record",
                    user.getUsername(), user.getId()
                );
                logger.error("❌ " + msg);
                throw new IllegalStateException(msg);
            }
        }
        logger.info("✅ Validación 1 OK: {} users con role=ORGANIZATION tienen Organization", orgUsers.size());

        // Invariante 2: Toda Organization debe tener User con role=ORGANIZATION
        organizationRepository.findAll().forEach(org -> {
            User user = userRepository.findById(org.getId()).orElse(null);
            if (user == null || user.getRole() != Role.ORGANIZATION) {
                String msg = String.format(
                    "INTEGRITY VIOLATION: Organization '%s' (id=%s) no tiene User correspondiente con role=ORGANIZATION",
                    org.getName(), org.getId()
                );
                logger.error("❌ " + msg);
                throw new IllegalStateException(msg);
            }
        });
        logger.info("✅ Validación 2 OK: Todas las Organizations tienen User correspondiente");

        logger.info("🎯 Integridad validada exitosamente");
    }
}
