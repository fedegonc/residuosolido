package com.residuosolido.app.controller;

import com.residuosolido.app.model.WasteSection;
import com.residuosolido.app.model.Category;
import com.residuosolido.app.repository.WasteSectionRepository;
import com.residuosolido.app.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.ArrayList;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/waste-sections")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWasteSectionController {

    @Autowired
    private WasteSectionRepository wasteSectionRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public String listWasteSections(Model model) {
        model.addAttribute("wasteSections", wasteSectionRepository.findAllByOrderByDisplayOrderAsc());
        return "admin/waste-sections";
    }

    @GetMapping("/new")
    public String newWasteSection(Model model) {
        model.addAttribute("wasteSection", new WasteSection());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/waste-section-form";
    }

    @GetMapping("/view/{id}")
    public String viewWasteSection(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            WasteSection wasteSection = wasteSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada"));
            model.addAttribute("wasteSection", wasteSection);
            return "admin/waste-section-view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar la sección: " + e.getMessage());
            return "redirect:/admin/waste-sections";
        }
    }

    @GetMapping("/edit/{id}")
    public String editWasteSection(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            WasteSection wasteSection = wasteSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada"));
            model.addAttribute("wasteSection", wasteSection);
            model.addAttribute("categories", categoryRepository.findAll());
            return "admin/waste-section-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar la sección: " + e.getMessage());
            return "redirect:/admin/waste-sections";
        }
    }

    @PostMapping("/save")
    public String saveWasteSection(@ModelAttribute WasteSection wasteSection, 
                                   @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Manejar las categorías seleccionadas
            if (categoryIds != null && !categoryIds.isEmpty()) {
                List<Category> selectedCategories = new ArrayList<>();
                for (Long categoryId : categoryIds) {
                    categoryRepository.findById(categoryId).ifPresent(selectedCategories::add);
                }
                wasteSection.setCategories(selectedCategories);
            } else {
                wasteSection.setCategories(new ArrayList<>());
            }
            
            wasteSectionRepository.save(wasteSection);
            redirectAttributes.addFlashAttribute("success", "Sección guardada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar la sección: " + e.getMessage());
        }
        return "redirect:/admin/waste-sections";
    }

    @PostMapping("/delete/{id}")
    public String deleteWasteSection(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            wasteSectionRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Sección eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la sección: " + e.getMessage());
        }
        return "redirect:/admin/waste-sections";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleWasteSectionStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            WasteSection wasteSection = wasteSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada"));
            wasteSection.setActive(!wasteSection.getActive());
            wasteSectionRepository.save(wasteSection);
            
            String statusMessage = wasteSection.getActive() ? "activada" : "desactivada";
            redirectAttributes.addFlashAttribute("success", "Sección " + statusMessage + " exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el estado de la sección: " + e.getMessage());
        }
        return "redirect:/admin/waste-sections";
    }
}
