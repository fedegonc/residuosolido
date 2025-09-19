package com.residuosolido.app.service;

import org.springframework.stereotype.Service;

@Service
public class ConfigService {
    // Servicio simplificado para casos de uso core
    private String heroImageUrl = "/images/hero-default.jpg"; // Temporal en memoria
    
    public String getAppName() {
        return "Residuos Sólidos";
    }
    
    public String getHeroImageUrl() {
        return heroImageUrl;
    }
    
    public void setHeroImageUrl(String url) {
        this.heroImageUrl = url;
    }
    
    public boolean isFeatureEnabled(String feature) {
        // Solo funcionalidades core habilitadas
        return "posts".equals(feature) || "requests".equals(feature) || "feedback".equals(feature);
    }
}
