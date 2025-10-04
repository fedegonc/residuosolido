package com.residuosolido.app.controller;

import com.residuosolido.app.model.AssistantConfig;
import com.residuosolido.app.repository.AssistantConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/assistant")
public class AssistantConfigController {

    @Autowired
    private AssistantConfigRepository assistantConfigRepository;

    @GetMapping
    public String showAssistantConfig(Model model) {
        AssistantConfig config = assistantConfigRepository
            .findFirstByIsActiveTrueOrderByUpdatedAtDesc()
            .orElse(new AssistantConfig());
        
        model.addAttribute("config", config);
        return "admin/assistant-config";
    }

    @PostMapping("/save")
    public String saveAssistantConfig(
            @RequestParam("systemPrompt") String systemPrompt,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Desactivar configuraciones anteriores
            assistantConfigRepository.findAll().forEach(c -> {
                c.setIsActive(false);
                assistantConfigRepository.save(c);
            });
            
            // Crear nueva configuración
            AssistantConfig config = new AssistantConfig();
            config.setSystemPrompt(systemPrompt);
            config.setIsActive(true);
            config.setUpdatedBy(authentication.getName());
            
            assistantConfigRepository.save(config);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Configuración del asistente guardada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al guardar la configuración: " + e.getMessage());
        }
        
        return "redirect:/admin/assistant";
    }
}
