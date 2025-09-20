package com.residuosolido.app.service;

import com.residuosolido.app.model.Config;
import com.residuosolido.app.repository.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigService {

    @Autowired
    private ConfigRepository configRepository;

    private static final String HERO_IMAGE_KEY = "hero_image_url";
    private static final String DEFAULT_HERO_IMAGE = "/images/hero-default.jpg";

    public String getAppName() {
        return "Residuos Sólidos";
    }

    public String getHeroImageUrl() {
        Optional<Config> config = configRepository.findByKey(HERO_IMAGE_KEY);
        return config.map(Config::getValue).orElse(DEFAULT_HERO_IMAGE);
    }

    public void setHeroImageUrl(String url) {
        Config config = configRepository.findByKey(HERO_IMAGE_KEY)
                .orElse(new Config(HERO_IMAGE_KEY, url));
        config.setValue(url);
        configRepository.save(config);
    }

    public boolean isFeatureEnabled(String feature) {
        // Solo funcionalidades core habilitadas
        return "posts".equals(feature) || "requests".equals(feature) || "feedback".equals(feature);
    }
}
