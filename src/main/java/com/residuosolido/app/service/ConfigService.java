package com.residuosolido.app.service;

import org.springframework.stereotype.Service;

@Service
public class ConfigService {
    // Servicio simplificado para casos de uso core
    
    public String getAppName() {
        return "Residuos Sólidos";
    }
    
    public String getHeroImageUrl() {
        return "/images/hero-default.jpg";
    }
    
    public boolean isFeatureEnabled(String feature) {
        // Solo funcionalidades core habilitadas
        return "posts".equals(feature) || "requests".equals(feature) || "feedback".equals(feature);
    }
}
