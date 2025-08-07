package com.residuosolido.app.controller;

import com.residuosolido.app.model.WasteSection;
import com.residuosolido.app.service.DataCleanupService;
import com.residuosolido.app.service.WasteSectionService;
import com.residuosolido.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    
    @GetMapping
    public String showWasteSections(Model model) {
        System.out.println("=== LISTANDO WASTE SECTIONS ===");
        List<WasteSection> sections = wasteSectionService.getAllOrderedByDisplayOrder();
        System.out.println("Secciones encontradas: " + sections.size());
        for (WasteSection section : sections) {
            System.out.println("- ID: " + section.getId() + ", Title: " + section.getTitle() + ", Active: " + section.getActive());
        }
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
                                   RedirectAttributes redirectAttributes) {
        try {
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
                                   RedirectAttributes redirectAttributes) {
        try {
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
