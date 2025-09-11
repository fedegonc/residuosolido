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

import java.util.List;
import java.util.Optional;

/**
 * Category Controller - Controlador unificado para gestión de categorías
 * 
 * Estructura:
 * - /categories - Endpoint público (solo lectura)
 * - /categories/{id} - Detalle de categoría con posts
 * - /admin/categories - Admin (CRUD completo)
 */
@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private PostService postService;

    // ========== MÉTODOS COMUNES ==========
    
    /**
     * Prepara modelo común para todas las vistas de categorías
     */
    private void prepareCategoryModel(Model model, List<Category> categories, String viewType) {
        model.addAttribute("categories", categories);
        model.addAttribute("totalCategories", categories.size());
        model.addAttribute("viewType", viewType);
    }
    
    /**
     * Maneja errores comunes en operaciones de categorías
     */
    private void handleCategoryError(Exception e, RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message + ": " + e.getMessage());
    }

    // ========== ADMIN ENDPOINTS ==========
    
    /**
     * Gestiona categorías (admin)
     * Soporta vistas: list, form, view
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/categories")
    public String adminCategories(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        
        // Valor por defecto: lista
        String viewType = "list";
        Category category = new Category();
        
        // Determinar tipo de vista según acción
        if ("new".equals(action)) {
            viewType = "form";
            // category ya está inicializado como nuevo
        } else if ("edit".equals(action) && id != null) {
            viewType = "form";
            category = categoryService.getCategoryById(id).orElse(new Category());
        } else if ("view".equals(action) && id != null) {
            viewType = "view";
            category = categoryService.getCategoryById(id).orElse(new Category());
        }
        
        // Preparar modelo común
        List<Category> allCategories = categoryService.findAll();
        prepareCategoryModel(model, allCategories, viewType);
        model.addAttribute("category", category);
        
        return "admin/categories";
    }

    /**
     * Procesa operaciones CRUD de categorías (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories")
    public String adminSaveCategory(
            @RequestParam(value = "action", required = false) String action,
            @ModelAttribute Category category,
            RedirectAttributes redirectAttributes) {
        
        
        return "redirect:/admin/categories";
    }

    /**
     * Elimina una categoría por ID (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories/delete/{id}")
    public String adminDeleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            // Verificar si tiene posts asociados antes de eliminar
            if (postService.countPostsByCategory(id) > 0) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "No se puede eliminar la categoría porque tiene posts asociados");
                return "redirect:/admin/categories";
            }
            
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Categoría eliminada correctamente");
        } catch (Exception e) {
            handleCategoryError(e, redirectAttributes, "Error al eliminar categoría");
        }
        
        return "redirect:/admin/categories";
    }

    // ========== PUBLIC ENDPOINTS ==========
    
    /**
     * Lista todas las categorías (público)
     */
    @GetMapping("/categories")
    public String publicCategories(Model model) {
        try {
            List<Category> activeCategories = categoryService.findAllActive();
            model.addAttribute("categories", activeCategories);
            return "public/categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar categorías: " + e.getMessage());
            return "public/categories";
        }
    }

    /**
     * Muestra detalle de una categoría con sus posts (público)
     */
    @GetMapping("/categories/{id}")
    public String publicCategoryDetail(@PathVariable Long id, Model model) {
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryById(id);
            if (categoryOpt.isEmpty() || !categoryOpt.get().isActive()) {
                return "redirect:/categories";
            }
            
            Category category = categoryOpt.get();
            model.addAttribute("category", category);
            model.addAttribute("posts", postService.getPostsByCategoryId(id));
            model.addAttribute("totalPosts", postService.countPostsByCategory(id));
            return "public/category-detail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar categoría: " + e.getMessage());
            return "redirect:/categories";
        }
    }
    
    
}
