package com.residuosolido.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Orquestador de carga de datos de prueba (dev/test).
 * Delega lógica de seed a DataSeeder para mantener Single Responsibility.
 */
@Configuration
@Profile({"dev"})
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    CommandLineRunner seedData(DataSeeder dataSeeder,
                               @Value("${app.seed:false}") boolean shouldSeed) {
        return args -> {
            logger.info("=== DataLoader: shouldSeed={} ===", shouldSeed);
            if (!shouldSeed) {
                logger.info("DataLoader: app.seed es false, omitiendo");
                return;
            }
            logger.info("DataLoader: delegando a DataSeeder.seedAllIfNeeded()");
            dataSeeder.seedAllIfNeeded();
            logger.info("✅ DataLoader: completado");
        };
    }
}
