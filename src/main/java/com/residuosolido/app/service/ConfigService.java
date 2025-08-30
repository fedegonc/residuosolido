package com.residuosolido.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConfigService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    private static final String HERO_IMAGE_KEY = "hero_image_url";

    // Fallback desde application.properties
    @Value("${app.hero.image.url:#{null}}")
    private String defaultHeroImageUrl;

    // Fallback hardcodeado
    private static final String DEFAULT_HERO_IMAGE = null; // null = usar gradiente

    public String getHeroImageUrl() {
        try {
            // 1. Intentar desde properties
            if (defaultHeroImageUrl != null && !defaultHeroImageUrl.trim().isEmpty()) {
                logger.debug("Using hero image from properties: {}", defaultHeroImageUrl);
                return defaultHeroImageUrl;
            }

            // 2. Intentar desde database (si está disponible)
            try {
                // Aquí iría la lógica de database si fuera necesario
                // Por ahora retornamos null para usar gradiente
                return DEFAULT_HERO_IMAGE;
            } catch (Exception e) {
                logger.warn("Database not available for hero image config, using default");
                return DEFAULT_HERO_IMAGE;
            }

        } catch (Exception e) {
            logger.error("Error getting hero image URL", e);
            return DEFAULT_HERO_IMAGE;
        }
    }

    public void setHeroImageUrl(String imageUrl) {
        // Método placeholder para futuras implementaciones
        logger.info("Hero image URL set to: {}", imageUrl);
        // Aquí iría la lógica para guardar en DB o properties
    }
}
