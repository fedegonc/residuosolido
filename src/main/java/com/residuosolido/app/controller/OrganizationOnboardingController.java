package com.residuosolido.app.controller;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.MaterialService;
import com.residuosolido.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para el onboarding de organizaciones recién registradas.
 * Maneja el flujo de completar perfil obligatorio para organizaciones.
 */
@Controller
public class OrganizationOnboardingController {

    @Autowired
    private UserService userService;

    @Autowired
    private MaterialService materialService;

    /**
     * Muestra el formulario de completar perfil para organizaciones.
     * Solo accesible para organizaciones con perfil incompleto.
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/acopio/completar-perfil")
    public String showCompleteProfileForm(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            
            // Si el perfil ya está completado, redirigir al dashboard
            if (currentUser.getProfileCompleted() != null && currentUser.getProfileCompleted()) {
                redirectAttributes.addFlashAttribute("infoMessage", "Tu perfil ya está completado");
                return "redirect:/acopio/inicio";
            }
            
            // Cargar todos los materiales activos disponibles
            List<Material> availableMaterials = materialService.findAllActive();
            
            model.addAttribute("organization", currentUser);
            model.addAttribute("availableMaterials", availableMaterials);
            
            return "org/complete-profile";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cargar el formulario: " + e.getMessage());
            return "redirect:/auth/login";
        }
    }

    /**
     * Procesa el formulario de completar perfil.
     * Valida y guarda la información obligatoria de la organización.
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/acopio/completar-perfil")
    public String completeProfile(
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) List<Long> materialIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            
            // Validaciones
            List<String> errors = new ArrayList<>();
            
            if (address == null || address.trim().isEmpty()) {
                errors.add("La dirección es obligatoria");
            } else if (address.trim().length() < 10) {
                errors.add("La dirección debe tener al menos 10 caracteres");
            }
            
            if (phone == null || phone.trim().isEmpty()) {
                errors.add("El teléfono es obligatorio");
            } else if (phone.trim().length() < 8) {
                errors.add("El teléfono debe tener al menos 8 dígitos");
            }
            
            if (materialIds == null || materialIds.isEmpty()) {
                errors.add("Debes seleccionar al menos un material que aceptas");
            }
            
            // Si hay errores, retornar al formulario
            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", String.join(". ", errors));
                return "redirect:/acopio/completar-perfil";
            }
            
            // Actualizar datos del usuario
            currentUser.setAddress(address.trim());
            currentUser.setPhone(phone.trim());
            
            // Actualizar materiales aceptados
            List<Material> selectedMaterials = new ArrayList<>();
            for (Long materialId : materialIds) {
                Material material = materialService.findById(materialId).orElse(null);
                if (material != null && material.getActive()) {
                    selectedMaterials.add(material);
                }
            }
            currentUser.setMaterials(selectedMaterials);
            
            // Marcar perfil como completado
            currentUser.setProfileCompleted(true);
            
            // Guardar cambios
            userService.updateUser(currentUser, null);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Perfil completado exitosamente! Bienvenido a EcoSolicitud");
            
            return "redirect:/acopio/inicio";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al completar perfil: " + e.getMessage());
            return "redirect:/acopio/completar-perfil";
        }
    }
}
