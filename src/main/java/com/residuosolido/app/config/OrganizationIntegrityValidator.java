package com.residuosolido.app.config;

import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Integridad referencial que Mongo no puede forzar: todo doc en
 * {@code organizations} debe tener su {@code users} con el mismo _id
 * (la cuenta a la que pertenece). La dirección inversa no se chequea —
 * no hay invariante que violar: un User sin Organization es simplemente
 * un ciudadano (el rol es derivado, no campo — ver User javadoc).
 */
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
    public void run(ApplicationArguments args) {
        logger.info("🔍 Validando integridad: Organization ↔ User");
        organizationRepository.findAll().forEach(org -> {
            if (!userRepository.existsById(org.getId())) {
                String msg = String.format(
                    "INTEGRITY VIOLATION: Organization '%s' (id=%s) no tiene User correspondiente",
                    org.getName(), org.getId()
                );
                logger.error("❌ " + msg);
                throw new IllegalStateException(msg);
            }
        });
        logger.info("✅ Todas las Organizations tienen su User");
    }
}
