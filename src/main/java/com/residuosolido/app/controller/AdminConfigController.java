package com.residuosolido.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.residuosolido.app.service.ConfigService;

import java.io.IOException;

@Controller
@RequestMapping("/admin/config")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {

    @Autowired
    private ConfigService configService;

    @GetMapping
    public String showConfig(Model model) {
        model.addAttribute("heroImage", configService.getHeroImageUrl());
        return "admin/config";
    }

    @PostMapping("/hero-image")
    public String updateHeroImage(@RequestParam("heroImageFile") MultipartFile file,
                                  RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Por favor selecciona una imagen");
                return "redirect:/admin/config";
            }

            String imageUrl = configService.saveHeroImage(file);
            redirectAttributes.addFlashAttribute("success", "Imagen de fondo actualizada correctamente");
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
        }
        
        return "redirect:/admin/config";
    }
}
