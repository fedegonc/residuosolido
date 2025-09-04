package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.Material;
import com.residuosolido.app.service.MaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/materials")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping
    public String list(@RequestParam(required = false) String action,
                       @RequestParam(required = false) Long id,
                       Model model) {
        List<Material> all = materialService.findAll();
        model.addAttribute("materials", all);
        model.addAttribute("totalMaterials", all != null ? all.size() : 0);

        if ("edit".equals(action) && id != null) {
            Optional<Material> material = materialService.findById(id);
            material.ifPresent(m -> model.addAttribute("material", m));
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", true);
            return "admin/materials";
        }

        if ("new".equals(action)) {
            model.addAttribute("material", new Material());
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", false);
            return "admin/materials";
        }

        model.addAttribute("viewType", "list");
        return "admin/materials";
    }

    @PostMapping
    public String save(@ModelAttribute Material material,
                       RedirectAttributes ra) {
        try {
            if (material.getId() != null) {
                // Editar material existente
                materialService.findById(material.getId()).ifPresent(existing -> {
                    existing.setName(material.getName());
                    existing.setDescription(material.getDescription());
                    existing.setCategory(material.getCategory());
                    existing.setActive(material.getActive());
                    materialService.save(existing);
                });
                ra.addFlashAttribute("successMessage", "Material actualizado correctamente");
            } else {
                // Crear nuevo material
                if (material.getActive() == null) material.setActive(true);
                materialService.save(material);
                ra.addFlashAttribute("successMessage", "Material creado correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/materials";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            materialService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Material eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/materials";
    }
}
