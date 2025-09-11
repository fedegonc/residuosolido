package com.residuosolido.app.controller;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Material Controller - Controlador unificado para gestión de materiales
 * 
 * Estructura:
 * - /materials - Endpoint base
 * 
 * Prefijos por rol:
 * - /admin/materials - Admin (CRUD completo)
 * - /org/materials - Organization (elegir qué materiales acepta)
 */
@Controller
public class MaterialController {

    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private UserService userService;

    // ========== MÉTODOS COMUNES ==========
    
    /**
     * Prepara modelo común para todas las vistas de materiales
     */
    private void prepareMaterialModel(Model model, List<Material> materials, String viewType) {
        model.addAttribute("materials", materials);
        model.addAttribute("totalMaterials", materials.size());
        model.addAttribute("viewType", viewType);
    }
    
    /**
     * Maneja errores comunes en operaciones de materiales
     */
    private void handleMaterialError(Exception e, RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message + ": " + e.getMessage());
    }

    // ========== ADMIN ENDPOINTS ==========
    
    /**
     * Gestiona materiales (admin)
     * Soporta vistas: list, form, view
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/materials")
    public String adminMaterials(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        
        // Valor por defecto: lista
        String viewType = "list";
        Material material = new Material();
        
        // Determinar tipo de vista según acción
        if ("new".equals(action)) {
            viewType = "form";
            // material ya está inicializado como nuevo
        } else if ("edit".equals(action) && id != null) {
            viewType = "form";
            material = materialService.findById(id).orElse(new Material());
        } else if ("view".equals(action) && id != null) {
            viewType = "view";
            material = materialService.findById(id).orElse(new Material());
        }
        
        // Preparar modelo común
        List<Material> allMaterials = materialService.findAll();
        prepareMaterialModel(model, allMaterials, viewType);
        model.addAttribute("material", material);
        
        return "admin/materials";
    }

    /**
     * Procesa operaciones CRUD de materiales (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/materials")
    public String adminSaveMaterial(
            @RequestParam(value = "action", required = false) String action,
            @ModelAttribute Material material,
            RedirectAttributes redirectAttributes) {
        try {
            // Operación según acción
            if ("delete".equals(action)) {
                materialService.deleteById(material.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Material eliminado correctamente");
            } else {
                // Crear o actualizar
                boolean isNew = material.getId() == null;
                materialService.save(material);
                String message = isNew ? "Material creado correctamente" : "Material actualizado correctamente";
                redirectAttributes.addFlashAttribute("successMessage", message);
            }
        } catch (Exception e) {
            handleMaterialError(e, redirectAttributes, "Error al procesar material");
        }
        
        return "redirect:/admin/materials";
    }

    /**
     * Elimina un material por ID (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/materials/delete/{id}")
    public String adminDeleteMaterial(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            materialService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Material eliminado correctamente");
        } catch (Exception e) {
            handleMaterialError(e, redirectAttributes, "Error al eliminar material");
        }
        
        return "redirect:/admin/materials";
    }

    // ========== ORGANIZATION ENDPOINTS ==========
    
    /**
     * Muestra materiales disponibles para organización
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/org/materials")
    public String orgMaterials(Authentication authentication, Model model) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            
            // Materiales disponibles y aceptados
            List<Material> availableMaterials = materialService.findAllActive();
            List<Material> acceptedMaterials = materialService.getAcceptedMaterialsByOrganization(currentUser);
            
            model.addAttribute("availableMaterials", availableMaterials);
            model.addAttribute("acceptedMaterials", acceptedMaterials);
            model.addAttribute("totalAvailable", availableMaterials.size());
            model.addAttribute("totalAccepted", acceptedMaterials.size());
            
            return "org/materials";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar materiales: " + e.getMessage());
            return "org/materials";
        }
    }

    /**
     * Actualiza lista completa de materiales aceptados
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/org/materials/update")
    public String orgUpdateMaterials(
            @RequestParam(value = "materialIds", required = false) List<Long> materialIds,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            
            if (materialIds == null || materialIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar al menos un material");
            } else {
                materialService.updateAcceptedMaterials(currentUser, materialIds);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Materiales actualizados correctamente. Ahora acepta " + materialIds.size() + " tipos de materiales");
            }
        } catch (Exception e) {
            handleMaterialError(e, redirectAttributes, "Error al actualizar materiales");
        }
        
        return "redirect:/org/materials";
    }

    /**
     * Alterna aceptación de un material específico
     */
    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/org/materials/toggle/{id}")
    public String orgToggleMaterial(
            @PathVariable Long id, 
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            boolean isAccepted = materialService.toggleMaterialAcceptance(currentUser, id);
            
            String action = isAccepted ? "agregado a" : "removido de";
            redirectAttributes.addFlashAttribute("successMessage", 
                "Material " + action + " su lista de materiales aceptados");
        } catch (Exception e) {
            handleMaterialError(e, redirectAttributes, "Error al cambiar material");
        }
        
        return "redirect:/org/materials";
    }
    
    /**
     * API para obtener materiales por organización
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/materials/organization/{id}")
    @ResponseBody
    public List<Material> getMaterialsByOrganization(@PathVariable Long id) {
        try {
            Optional<User> organization = userService.findById(id);
            if (organization.isPresent() && organization.get().getRole() == Role.ORGANIZATION) {
                return materialService.getAcceptedMaterialsByOrganization(organization.get());
            }
        } catch (Exception e) {
            // Log error pero retornar lista vacía
            System.err.println("Error obteniendo materiales: " + e.getMessage());
        }
        return List.of();
    }
}
