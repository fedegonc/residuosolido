package com.residuosolido.app.controller;

import com.residuosolido.app.model.WasteSection;
import com.residuosolido.app.repository.WasteSectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping
    public String listWasteSections(Model model) {
        model.addAttribute("wasteSections", wasteSectionRepository.findAllByOrderByDisplayOrderAsc());
        return "admin/waste-sections";
    }

    @GetMapping("/new")
    public String newWasteSection(Model model) {
        model.addAttribute("wasteSection", new WasteSection());
        return "admin/waste-section-form";
    }

    @GetMapping("/edit/{id}")
    public String editWasteSection(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            WasteSection wasteSection = wasteSectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sección no encontrada"));
            model.addAttribute("wasteSection", wasteSection);
            model.addAttribute("isEdit", true);
            return "admin/waste-section-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar la sección: " + e.getMessage());
            return "redirect:/admin/waste-sections";
        }
    }

    @PostMapping("/save")
    public String saveWasteSection(@ModelAttribute WasteSection wasteSection, RedirectAttributes redirectAttributes) {
        try {
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
