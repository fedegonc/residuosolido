package com.residuosolido.app.controller;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.service.CategoryService;
import com.residuosolido.app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Category Controller - Maneja categorías por rol
 * Endpoints:
 * - /admin/categories → ROLE_ADMIN (CRUD completo)
 * - /categories → Público (solo lectura)
 */
@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private PostService postService;

    // ========== ADMIN ENDPOINTS ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/categories")
    public String adminCategories(@RequestParam(value = "action", required = false) String action,
                                 @RequestParam(value = "id", required = false) Long id,
                                 Model model) {
        
        String viewType = "list";
        Category category = new Category();
        
        if ("new".equals(action)) {
            viewType = "form";
            // category ya está inicializado
        } else if ("edit".equals(action) && id != null) {
            viewType = "form";
            category = categoryService.getCategoryById(id).orElse(new Category());
        } else if ("view".equals(action) && id != null) {
            viewType = "view";
            category = categoryService.getCategoryById(id).orElse(new Category());
        }
        
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("category", category);
        model.addAttribute("viewType", viewType);
        model.addAttribute("totalCategories", categoryService.count());
        
        return "admin/categories";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories")
    public String adminSaveCategory(@RequestParam("action") String action,
                                   @ModelAttribute Category category,
                                   RedirectAttributes redirectAttributes) {
        try {
            if ("delete".equals(action)) {
                categoryService.deleteById(category.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Categoría eliminada correctamente");
            } else {
                categoryService.save(category);
                String message = category.getId() == null ? "Categoría creada correctamente" : "Categoría actualizada correctamente";
                redirectAttributes.addFlashAttribute("successMessage", message);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar categoría: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }

    // ========== PUBLIC ENDPOINTS ==========
    @GetMapping("/categories")
    public String publicCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "public/categories";
    }

    @GetMapping("/categories/{id}")
    public String publicCategoryDetail(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id).orElse(null);
        if (category == null) {
            return "redirect:/categories";
        }
        
        model.addAttribute("category", category);
        model.addAttribute("posts", postService.getPostsByCategoryId(id));
        return "public/category-detail";
    }
}
