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

/**
 * Material Controller - Maneja materiales por rol
 * Endpoints:
 * - /admin/materials → ROLE_ADMIN (CRUD completo)
 * - /org/materials → ROLE_ORGANIZATION (elegir qué materiales acepta)
 */
@Controller
public class MaterialController {

    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private UserService userService;

    // ========== ADMIN ENDPOINTS ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/materials")
    public String adminMaterials(@RequestParam(value = "action", required = false) String action,
                                @RequestParam(value = "id", required = false) Long id,
                                Model model) {
        
        String viewType = "list";
        Material material = new Material();
        
        if ("new".equals(action)) {
            viewType = "form";
            // material ya está inicializado
        } else if ("edit".equals(action) && id != null) {
            viewType = "form";
            material = materialService.findById(id).orElse(new Material());
        } else if ("view".equals(action) && id != null) {
            viewType = "view";
            material = materialService.findById(id).orElse(new Material());
        }
        
        model.addAttribute("materials", materialService.findAll());
        model.addAttribute("material", material);
        model.addAttribute("viewType", viewType);
        model.addAttribute("totalMaterials", materialService.count());
        
        return "admin/materials";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/materials")
    public String adminSaveMaterial(@RequestParam("action") String action,
                                   @ModelAttribute Material material,
                                   RedirectAttributes redirectAttributes) {
        try {
            if ("delete".equals(action)) {
                materialService.deleteById(material.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Material eliminado correctamente");
            } else {
                materialService.save(material);
                String message = material.getId() == null ? "Material creado correctamente" : "Material actualizado correctamente";
                redirectAttributes.addFlashAttribute("successMessage", message);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar material: " + e.getMessage());
        }
        
        return "redirect:/admin/materials";
    }

    // ========== ORGANIZATION ENDPOINTS ==========
    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/org/materials")
    public String orgMaterials(Authentication authentication, Model model) {
        User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
        
        // Todos los materiales disponibles (creados por admin)
        List<Material> availableMaterials = materialService.findAll();
        
        // Materiales que acepta esta organización
        List<Material> acceptedMaterials = materialService.getAcceptedMaterialsByOrganization(currentUser);
        
        model.addAttribute("availableMaterials", availableMaterials);
        model.addAttribute("acceptedMaterials", acceptedMaterials);
        model.addAttribute("totalAvailable", availableMaterials.size());
        model.addAttribute("totalAccepted", acceptedMaterials.size());
        
        return "org/materials";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/org/materials/update")
    public String orgUpdateMaterials(@RequestParam(value = "materialIds", required = false) List<Long> materialIds,
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
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar materiales: " + e.getMessage());
        }
        
        return "redirect:/org/materials";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/org/materials/toggle/{id}")
    public String orgToggleMaterial(@PathVariable Long id, 
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findAuthenticatedUserByUsername(authentication.getName());
            boolean isAccepted = materialService.toggleMaterialAcceptance(currentUser, id);
            
            String action = isAccepted ? "agregado a" : "removido de";
            redirectAttributes.addFlashAttribute("successMessage", 
                "Material " + action + " su lista de materiales aceptados");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al cambiar material: " + e.getMessage());
        }
        
        return "redirect:/org/materials";
    }
}
