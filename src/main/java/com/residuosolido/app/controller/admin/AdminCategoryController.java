package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(required = false) String action,
                       @RequestParam(required = false) Long id,
                       Model model) {
        List<Category> all = categoryService.findAll();
        model.addAttribute("categories", all);
        model.addAttribute("totalCategories", all != null ? all.size() : 0);

        if ("edit".equals(action) && id != null) {
            Optional<Category> category = categoryService.getCategoryById(id);
            category.ifPresent(c -> model.addAttribute("category", c));
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", true);
            return "admin/categories";
        }

        if ("new".equals(action)) {
            model.addAttribute("category", new Category());
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", false);
            return "admin/categories";
        }

        model.addAttribute("viewType", "list");
        return "admin/categories";
    }

    @PostMapping
    public String save(@ModelAttribute Category category,
                       RedirectAttributes ra) {
        try {
            if (category.getId() != null) {
                // Editar categoría existente
                categoryService.getCategoryById(category.getId()).ifPresent(existing -> {
                    existing.setName(category.getName());
                    existing.setDescription(category.getDescription());
                    existing.setImageUrl(category.getImageUrl());
                    existing.setDisplayOrder(category.getDisplayOrder());
                    existing.setActive(category.getActive());
                    categoryService.save(existing);
                });
                ra.addFlashAttribute("successMessage", "Categoría actualizada correctamente");
            } else {
                // Crear nueva categoría
                if (category.getActive() == null) category.setActive(true);
                categoryService.save(category);
                ra.addFlashAttribute("successMessage", "Categoría creada correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Categoría eliminada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
