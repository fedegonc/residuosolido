package com.residuosolido.app.service;

import com.residuosolido.app.model.SiteConfig;
import com.residuosolido.app.repository.SiteConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
    
    private static final String HERO_IMAGE_KEY = "hero_image_url";
    
    @Autowired
    private SiteConfigRepository siteConfigRepository;

    public String getHeroImageUrl() {
        return siteConfigRepository.findByKey(HERO_IMAGE_KEY)
                .map(SiteConfig::getValue)
                .orElse(null);
    }
    
    public void saveHeroImageUrl(String imageUrl) {
        SiteConfig config = siteConfigRepository.findByKey(HERO_IMAGE_KEY)
                .orElse(new SiteConfig(HERO_IMAGE_KEY, imageUrl));
        config.setValue(imageUrl);
        siteConfigRepository.save(config);
    }
}
