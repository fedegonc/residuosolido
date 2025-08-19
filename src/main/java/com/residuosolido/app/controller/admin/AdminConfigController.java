package com.residuosolido.app.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.residuosolido.app.service.ConfigService;
import com.residuosolido.app.service.CloudinaryService;

@Controller
@RequestMapping("/admin/config")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {

    @Autowired
    private ConfigService configService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @GetMapping
    public String showConfig(Model model) {
        model.addAttribute("heroImage", configService.getHeroImageUrl());
        return "admin/config";
    }

    @PostMapping("/hero-image")
    public String updateHeroImage(@RequestParam("heroImageFile") MultipartFile file,
                                  RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Por favor selecciona una imagen");
            return "redirect:/admin/config";
        }

        try {
            String imageUrl = cloudinaryService.uploadFile(file);
            configService.saveHeroImageUrl(imageUrl);
            redirectAttributes.addFlashAttribute("success", "Imagen subida exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir imagen: " + e.getMessage());
        }
        
        return "redirect:/admin/config";
    }
}
