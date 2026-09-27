package com.residuosolido.app.browser;

import com.residuosolido.app.config.DataLoader;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.RequestRepository;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Carga datos de prueba para los tests de navegador.
 * Borra y recarga el seed para garantizar estado consistente.
 */
@TestConfiguration
public class BrowserTestSeed {

    private static final Logger log = LoggerFactory.getLogger(BrowserTestSeed.class);

    @Bean
    CommandLineRunner browserTestSeedData(UserRepository userRepository,
                                          OrganizationRepository organizationRepository,
                                          RequestRepository requestRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("=== BrowserTestSeed: limpiando BD ===");
            userRepository.deleteAll();
            organizationRepository.deleteAll();
            requestRepository.deleteAll();
            log.info("=== BrowserTestSeed: cargando seed data ===");
            DataLoader.seedAll(userRepository, organizationRepository, requestRepository, passwordEncoder);
            log.info("=== BrowserTestSeed: seed completado, users={}, requests={} ===",
                    userRepository.count(), requestRepository.count());
        };
    }
}
