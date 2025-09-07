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

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/admin/categories")
    public String listCategories(@RequestParam(required = false) String action,
                                 @RequestParam(required = false) Long id,
                                 Model model) {
        if ("new".equals(action)) {
            categoryService.prepareCategoryFormView(model, null, false);
        } else if ("edit".equals(action) && id != null) {
            categoryService.prepareCategoryFormView(model, id, true);
        } else {
            categoryService.prepareCategoryListView(model);
        }
        return "admin/categories";
    }

    @PostMapping("/admin/categories")
    public String saveCategory(@ModelAttribute Category category,
                              RedirectAttributes ra) {
        try {
            String message = categoryService.saveCategory(category);
            ra.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Categoría eliminada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
