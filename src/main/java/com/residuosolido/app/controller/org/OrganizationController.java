package com.residuosolido.app.controller.org;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
 

@Controller
@RequestMapping("/org")
public class OrganizationController {

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/dashboard")
    public String orgDashboard(Model model) {
        // Datos mínimos para la vista (podemos ampliar luego)
        model.addAttribute("totalOrganizations", 0);
        return "org/dashboard";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/materials")
    public String materialsPage(Model model) {
        // Lista hardcoded de materiales disponibles
        List<String> availableMaterials = Arrays.asList(
            "Papel y Cartón",
            "Plástico PET",
            "Plástico HDPE", 
            "Vidrio",
            "Aluminio",
            "Chatarra Metálica",
            "Baterías",
            "Electrónicos",
            "Textiles",
            "Aceite de Cocina",
            "Neumáticos",
            "Madera"
        );
        
        // Materiales actualmente seleccionados por la organización (hardcoded por ahora)
        List<String> selectedMaterials = Arrays.asList(
            "Papel y Cartón",
            "Plástico PET",
            "Vidrio",
            "Aluminio"
        );
        
        model.addAttribute("availableMaterials", availableMaterials);
        model.addAttribute("selectedMaterials", selectedMaterials);
        return "org/materials";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/materials/update")
    public String updateMaterials(@RequestParam(value = "materials", required = false) List<String> materials,
                                 RedirectAttributes redirectAttributes) {
        // Por ahora solo simulamos la actualización
        if (materials == null || materials.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos un material");
        } else {
            redirectAttributes.addFlashAttribute("success", 
                "Materiales actualizados correctamente. Ahora recibes: " + String.join(", ", materials));
        }
        return "redirect:/org/materials";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @GetMapping("/profile")
    public String profilePage(Model model) {
        // Lista hardcoded de materiales disponibles
        List<String> availableMaterials = Arrays.asList(
            "Papel y Cartón",
            "Plástico PET",
            "Plástico HDPE", 
            "Vidrio",
            "Aluminio",
            "Chatarra Metálica",
            "Baterías",
            "Electrónicos",
            "Textiles",
            "Aceite de Cocina",
            "Neumáticos",
            "Madera"
        );
        
        // Datos actuales de la organización (hardcoded por ahora)
        model.addAttribute("currentZone", "Barrio Centro (Rivera)");
        model.addAttribute("currentPhone", "098 123 456");
        model.addAttribute("availableMaterials", availableMaterials);
        model.addAttribute("selectedMaterials", Arrays.asList("Papel y Cartón", "Plástico PET", "Vidrio", "Aluminio"));
        
        return "org/profile";
    }

    @PreAuthorize("hasRole('ORGANIZATION')")
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("zone") String zone,
                               @RequestParam("phone") String phone,
                               @RequestParam(value = "materials", required = false) List<String> materials,
                               RedirectAttributes redirectAttributes) {
        // Validaciones básicas
        if (zone == null || zone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La zona es obligatoria");
            return "redirect:/org/profile";
        }
        
        if (phone == null || phone.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El teléfono es obligatorio");
            return "redirect:/org/profile";
        }
        
        if (materials == null || materials.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos un material");
            return "redirect:/org/profile";
        }
        
        // Por ahora solo simulamos la actualización
        redirectAttributes.addFlashAttribute("success", 
            "Perfil actualizado correctamente. Zona: " + zone + ", Tel: " + phone + 
            ", Materiales: " + String.join(", ", materials));
        
        return "redirect:/org/profile";
    }
}
