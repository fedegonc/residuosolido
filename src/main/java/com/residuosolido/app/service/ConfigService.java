package com.residuosolido.app.service;

import com.residuosolido.app.model.HeroImage;
import com.residuosolido.app.repository.HeroImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ConfigService {

    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private HeroImageRepository heroImageRepository;

    public String getHeroImageUrl() {
        return heroImageRepository.findByIsActiveTrue()
                .map(HeroImage::getImageUrl)
                .orElse("");
    }

    public List<HeroImage> getAllHeroImages() {
        return heroImageRepository.findAllByOrderByUploadDateDesc();
    }

    public String saveHeroImage(MultipartFile file) throws IOException {
        // Subir imagen a Cloudinary
        String cloudinaryUrl = cloudinaryService.uploadFile(file);
        
        // Desactivar imagen actual
        heroImageRepository.findByIsActiveTrue()
                .ifPresent(img -> {
                    img.setActive(false);
                    heroImageRepository.save(img);
                });
        
        // Crear nueva imagen activa
        HeroImage newImage = new HeroImage(cloudinaryUrl);
        newImage.setActive(true);
        heroImageRepository.save(newImage);
        
        return cloudinaryUrl;
    }
    
    public void setActiveImage(Long imageId) {
        // Desactivar todas
        heroImageRepository.findByIsActiveTrue()
                .ifPresent(img -> {
                    img.setActive(false);
                    heroImageRepository.save(img);
                });
        
        // Activar seleccionada
        heroImageRepository.findById(imageId)
                .ifPresent(img -> {
                    img.setActive(true);
                    heroImageRepository.save(img);
                });
    }
    
    public void deleteHeroImage(Long imageId) {
        heroImageRepository.deleteById(imageId);
    }
}
