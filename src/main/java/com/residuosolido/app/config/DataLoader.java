package com.residuosolido.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Orquestador de carga de datos de prueba (dev/test).
 * Usa .seed marker file en lugar de endpoint HTTP destructivo.
 *
 * Flujo:
 * 1. Arranque: si .seed no existe, carga datos automáticamente
 * 2. Después de seed: crea .seed marker
 * 3. Para resetear: rm .seed, restart app
 */
@Configuration
@Profile({"dev"})
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private static final Path SEED_MARKER = Paths.get(".seed");

    @Bean
    CommandLineRunner seedData(DataSeeder dataSeeder,
                               @Value("${app.seed:false}") boolean shouldSeed) {
        return args -> {
            boolean markerExists = Files.exists(SEED_MARKER);
            logger.info("=== DataLoader: shouldSeed={}, marker exists={} ===", shouldSeed, markerExists);

            if (markerExists) {
                logger.info("DataLoader: .seed marker encontrado, omitiendo seed (usa: rm .seed para resetear)");
                return;
            }

            if (!shouldSeed) {
                logger.info("DataLoader: app.seed es false, omitiendo seed");
                return;
            }

            logger.info("DataLoader: delegando a DataSeeder.seedAllIfNeeded()");
            dataSeeder.seedAllIfNeeded();

            try {
                Files.createFile(SEED_MARKER);
                logger.info("✅ DataLoader: completado, .seed marker creado");
            } catch (Exception e) {
                logger.warn("DataLoader: no pude crear .seed marker: {}", e.getMessage());
            }
        };
    }
}
