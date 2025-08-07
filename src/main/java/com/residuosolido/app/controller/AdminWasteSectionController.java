package com.residuosolido.app.controller;

import com.residuosolido.app.model.WasteSection;
import com.residuosolido.app.service.CloudinaryService;
import com.residuosolido.app.service.DataCleanupService;
import com.residuosolido.app.service.WasteSectionService;
import com.residuosolido.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/admin/waste-sections")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWasteSectionController {
    
    @Autowired
    private DataCleanupService dataCleanupService;
    
    @Autowired
    private WasteSectionService wasteSectionService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @GetMapping
    public String showWasteSections(Model model) {
        List<WasteSection> sections = wasteSectionService.getAllOrderedByDisplayOrder();
        model.addAttribute("wasteSections", sections);
        return "admin/waste-sections";
    }
    
    @PostMapping("/cleanup")
    public String cleanupData(RedirectAttributes redirectAttributes) {
        try {
            dataCleanupService.cleanupWasteSectionCategories();
            redirectAttributes.addFlashAttribute("success", "Datos limpiados correctamente. FK violations resueltas.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al limpiar datos: " + e.getMessage());
        }
        return "redirect:/admin/waste-sections";
    }
    
    @GetMapping("/new")
    public String showNewWasteSectionForm(Model model) {
        model.addAttribute("wasteSection", new WasteSection());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/waste-section-form";
    }
    
    @PostMapping("/new")
    public String createWasteSection(@ModelAttribute WasteSection wasteSection,
                                   @RequestParam(value = "categoryIds", required = false) Long[] categoryIds,
                                   @RequestParam(value = "image", required = false) MultipartFile image,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Asignar valores por defecto para campos requeridos
            
            if (wasteSection.getActionText() == null || wasteSection.getActionText().trim().isEmpty()) {
                wasteSection.setActionText("Ver más"); // Texto de acción por defecto
            }
            if (wasteSection.getDisplayOrder() == null) {
                wasteSection.setDisplayOrder(1); // Orden por defecto
            }
            
            // Procesar imagen si se proporciona
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(image);
                wasteSection.setImageUrl(imageUrl);
            } else {
                // Imagen por defecto si no se proporciona
                wasteSection.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
            }
            
            wasteSectionService.createWasteSection(wasteSection, categoryIds);
            redirectAttributes.addFlashAttribute("success", "Sección creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear sección: " + e.getMessage());
        }
        return "redirect:/admin/waste-sections";
    }
    
    @GetMapping("/{id}/edit")
    public String showEditWasteSectionForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            WasteSection wasteSection = wasteSectionService.getWasteSectionById(id);
            model.addAttribute("wasteSection", wasteSection);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("selectedCategories", wasteSection.getCategories());
            return "admin/waste-section-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar la sección: " + e.getMessage());
            return "redirect:/admin/waste-sections";
        }
    }
    
    @PostMapping("/{id}/edit")
    public String updateWasteSection(@PathVariable Long id, 
                                   @ModelAttribute WasteSection wasteSection,
                                   @RequestParam(value = "categoryIds", required = false) Long[] categoryIds,
                                   @RequestParam(value = "image", required = false) MultipartFile image,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Procesar imagen si se proporciona
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(image);
                wasteSection.setImageUrl(imageUrl);
            }
            
            wasteSectionService.updateWasteSection(id, wasteSection, categoryIds);
            redirectAttributes.addFlashAttribute("success", "Sección actualizada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar sección: " + e.getMessage());
        }
        return "redirect:/admin/waste-sections";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteWasteSection(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            wasteSectionService.deleteWasteSection(id);
            redirectAttributes.addFlashAttribute("success", "Sección eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar sección: " + e.getMessage());
        }
        return "redirect:/admin/waste-sections";
    }
}
