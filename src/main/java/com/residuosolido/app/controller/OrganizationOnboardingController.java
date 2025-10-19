package com.residuosolido.app.controller;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.MaterialService;
import com.residuosolido.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador para el onboarding de organizaciones recién registradas.
 * Maneja el flujo de completar perfil obligatorio para organizaciones.
 */
@Controller
public class OrganizationOnboardingController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationOnboardingController.class);

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
            logger.info("========== GET /acopio/completar-perfil ==========");
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            logger.info("Usuario: username={}, id={}, profileCompleted={}", currentUser.getUsername(), currentUser.getId(), currentUser.getProfileCompleted());
            
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
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {
        
        try {
            logger.info("========== POST /acopio/completar-perfil ==========");
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            logger.info("Usuario ANTES de cambios: username={}, id={}, profileCompleted={}", currentUser.getUsername(), currentUser.getId(), currentUser.getProfileCompleted());
            
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
            
            // IMPORTANTE: Marcar como completado ANTES de guardar
            logger.info("========================================");
            logger.info("=== MARCANDO PERFIL COMO COMPLETADO ===");
            logger.info("========================================");
            logger.info("Usuario: {}", currentUser.getUsername());
            logger.info("ID: {}", currentUser.getId());
            logger.info("profileCompleted ANTES: {}", currentUser.getProfileCompleted());
            currentUser.setProfileCompleted(true);
            logger.info("profileCompleted DESPUÉS de setear: {}", currentUser.getProfileCompleted());
            logger.info("----------------------------------------");
            
            // Guardar TODO junto (dirección, teléfono, materiales, profileCompleted)
            logger.info("========== GUARDANDO USUARIO ==========");
            User savedUser = userService.saveDirectly(currentUser);
            logger.info("Usuario guardado. profileCompleted retornado: {}", savedUser.getProfileCompleted());
            
            // Usar también query nativa como backup
            logger.info("========== EJECUTANDO QUERY NATIVA COMO BACKUP ==========");
            userService.markProfileAsCompleted(currentUser.getId());
            
            // Verificar TRES veces para estar seguros
            logger.info("========================================");
            logger.info("=== VERIFICACIÓN FINAL DESDE BD ===");
            logger.info("========================================");
            
            User verificacion1 = userService.findById(currentUser.getId()).orElse(null);
            if (verificacion1 != null) {
                logger.info("✓ Verificación 1 (findById):");
                logger.info("  - ID: {}", verificacion1.getId());
                logger.info("  - Username: {}", verificacion1.getUsername());
                logger.info("  - profileCompleted: {}", verificacion1.getProfileCompleted());
            } else {
                logger.error("✗ Verificación 1: Usuario no encontrado");
            }
            
            logger.info("----------------------------------------");
            
            User verificacion2 = userService.findAuthenticatedUserByUsername(authentication.getName());
            logger.info("✓ Verificación 2 (findByUsername):");
            logger.info("  - ID: {}", verificacion2.getId());
            logger.info("  - Username: {}", verificacion2.getUsername());
            logger.info("  - profileCompleted: {}", verificacion2.getProfileCompleted());
            logger.info("========================================");
            
            // CRÍTICO: Invalidar sesión para forzar recarga desde BD en próximo login
            logger.info("========== INVALIDANDO SESIÓN PARA REFRESCAR ESTADO ==========");
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, authentication);
            logger.info("Sesión invalidada. Usuario será redirigido a login.");
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Perfil completado exitosamente! Por favor, inicia sesión nuevamente para acceder al sistema.");
            
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al completar perfil: " + e.getMessage());
            return "redirect:/acopio/completar-perfil";
        }
    }
}
