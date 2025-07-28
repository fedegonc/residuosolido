package com.residuosolido.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ConfigService {

    @Autowired
    private CloudinaryService cloudinaryService;
    
    private String heroImageUrl = null; // Sin imagen por defecto

    public String getHeroImageUrl() {
        return heroImageUrl;
    }

    public String saveHeroImage(MultipartFile file) throws IOException {
        // Subir imagen a Cloudinary
        String cloudinaryUrl = cloudinaryService.uploadFile(file);
        
        // Actualizar URL
        heroImageUrl = cloudinaryUrl;
        return heroImageUrl;
    }
}
