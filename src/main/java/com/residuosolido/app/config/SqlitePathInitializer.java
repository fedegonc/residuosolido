package com.residuosolido.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Garantiza que exista el directorio del archivo SQLite al arrancar.
 * Útil en Render cuando se usa un path dentro del proyecto, por ejemplo:
 * SPRING_DATASOURCE_URL=jdbc:sqlite:/opt/render/project/src/data/residuosolido.db
 */
@Component
@Order(0)
public class SqlitePathInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SqlitePathInitializer.class);

    @Override
    public void run(ApplicationArguments args) {
        String url = System.getenv("SPRING_DATASOURCE_URL");
        if (url == null || !url.startsWith("jdbc:sqlite:")) {
            // Usar el valor por defecto de application.properties
            url = "jdbc:sqlite:./data/residuosolido.db";
        }

        try {
            String path = url.substring("jdbc:sqlite:".length());
            File dbFile = new File(path);
            File parentDir = dbFile.getAbsoluteFile().getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (created) {
                    log.info("[SQLite] Creado directorio para DB: {}", parentDir.getAbsolutePath());
                }
            }
            log.info("[SQLite] DB path eficaz: {}", dbFile.getAbsolutePath());
        } catch (Exception ex) {
            log.warn("[SQLite] No se pudo asegurar el directorio de la DB: {}", ex.getMessage());
        }
    }
}
