package com.residuosolido.app.controller;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.model.Organization;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.MaterialRepository;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class MaterialController {

    private final MaterialRepository materialRepository;
    private final OrganizationRepository organizationRepository;
    private final UserService userService;

    public MaterialController(MaterialRepository materialRepository, 
                             OrganizationRepository organizationRepository,
                             UserService userService) {
        this.materialRepository = materialRepository;
        this.organizationRepository = organizationRepository;
        this.userService = userService;
    }

    @GetMapping("/admin/materials")
    @PreAuthorize("hasRole('ADMIN')")
    public String listMaterials(Model model) {
        model.addAttribute("materials", materialRepository.findAll());
        model.addAttribute("pageTitle", "Gestión de Materiales");
        return "admin/materials";
    }

    @GetMapping("/admin/materials/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newMaterialForm(Model model) {
        model.addAttribute("material", new Material());
        model.addAttribute("pageTitle", "Nuevo Material");
        return "admin/material-form";
    }

    @GetMapping("/admin/materials/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editMaterialForm(@PathVariable Long id, Model model) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material no encontrado"));
        model.addAttribute("material", material);
        model.addAttribute("pageTitle", "Editar Material");
        return "admin/material-form";
    }

    @PostMapping("/admin/materials/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveMaterial(@ModelAttribute Material material, RedirectAttributes redirectAttributes) {
        materialRepository.save(material);
        redirectAttributes.addFlashAttribute("success", "Material guardado correctamente");
        return "redirect:/admin/materials";
    }

    @GetMapping("/org/materials")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public String organizationMaterials(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Organization organization = findOrganizationByUser(user);
        
        if (organization == null) {
            return "redirect:/org/dashboard";
        }
        
        List<Material> allMaterials = materialRepository.findByActiveTrue();
        List<Material> acceptedMaterials = materialRepository.findByOrganizationsId(organization.getId());
        
        model.addAttribute("organization", organization);
        model.addAttribute("allMaterials", allMaterials);
        model.addAttribute("acceptedMaterials", acceptedMaterials);
        model.addAttribute("pageTitle", "Materiales Aceptados");
        
        return "org/materials";
    }
    
    @PostMapping("/org/materials/update")
    @PreAuthorize("hasRole('ORGANIZATION')")
    public String updateOrganizationMaterials(@RequestParam List<Long> materialIds, 
                                             Authentication authentication,
                                             RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Organization organization = findOrganizationByUser(user);
        
        if (organization == null) {
            return "redirect:/org/dashboard";
        }
        
        List<Material> selectedMaterials = materialRepository.findAllById(materialIds);
        organization.setAcceptedMaterials(selectedMaterials);
        organizationRepository.save(organization);
        
        redirectAttributes.addFlashAttribute("success", "Materiales actualizados correctamente");
        return "redirect:/org/materials";
    }
    
    private Organization findOrganizationByUser(User user) {
        // Buscar organización por nombre de usuario (asumiendo que el username es único)
        Optional<Organization> org = organizationRepository.findAll().stream()
                .filter(o -> o.getName().equals(user.getUsername()))
                .findFirst();
        return org.orElse(null);
    }
}
