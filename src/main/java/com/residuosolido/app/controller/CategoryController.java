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
            @RequestParam(value = "q", required = false) String query,
            Model model) {
        
        // Valor por defecto: lista
        String viewType = "list";
        Category category = new Category();
        
        // Determinar tipo de vista según acción
        if ("new".equals(action)) {
            viewType = "form";
            model.addAttribute("isEdit", false);
            // category ya está inicializado como nuevo
        } else if ("edit".equals(action) && id != null) {
            viewType = "form";
            category = categoryService.getCategoryById(id).orElse(new Category());
            model.addAttribute("isEdit", true);
        } else if ("view".equals(action) && id != null) {
            viewType = "view";
            category = categoryService.getCategoryById(id).orElse(new Category());
        }
        
        // Preparar modelo común
        List<Category> allCategories = categoryService.findAll();
        // Filtro de búsqueda
        if (query != null && !query.trim().isEmpty()) {
            String q = query.trim().toLowerCase();
            allCategories = allCategories.stream().filter(c -> {
                String name = c.getName() != null ? c.getName().toLowerCase() : "";
                String description = c.getDescription() != null ? c.getDescription().toLowerCase() : "";
                return name.contains(q) || description.contains(q);
            }).toList();
        }
        prepareCategoryModel(model, allCategories, viewType);
        model.addAttribute("category", category);
        model.addAttribute("query", query);
        
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
        try {
            if ("delete".equals(action) && category.getId() != null) {
                // Redirigir a endpoint dedicado de delete
                return "redirect:/admin/categories/delete/" + category.getId();
            }
            String message = categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            handleCategoryError(e, redirectAttributes, "Error al procesar categoría");
        }
        return "redirect:/admin/categories";
    }

    // ========== HTMX ENDPOINTS ==========

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/categories/form-demo")
    public String getCategoryFormDemo(Model model) {
        Category demo = new Category();
        demo.setName("Residuos Orgánicos");
        demo.setDescription("Restos de comida y materiales biodegradables aptos para compostaje.");
        demo.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
        demo.setDisplayOrder(10);
        demo.setActive(true);
        model.addAttribute("category", demo);
        return "admin/categories :: categoryFormFields";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/categories/form-demo")
    public String createCategoryDemo(RedirectAttributes redirectAttributes) {
        try {
            Category demo = new Category();
            demo.setName("Categoría de Prueba");
            demo.setDescription("Categoría creada desde el botón 'Completar campos' para demo.");
            demo.setImageUrl("https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg");
            demo.setDisplayOrder(99);
            demo.setActive(true);
            categoryService.save(demo);
            redirectAttributes.addFlashAttribute("successMessage", "Categoría de prueba creada exitosamente");
        } catch (Exception e) {
            handleCategoryError(e, redirectAttributes, "Error al crear categoría de prueba");
        }
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
    @GetMapping("/categorias")
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
    @GetMapping("/categorias/{id}")
    public String publicCategoryDetail(@PathVariable Long id, Model model) {
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryById(id);
            if (categoryOpt.isEmpty() || !categoryOpt.get().isActive()) {
                return "redirect:/categorias";
            }
            
            Category category = categoryOpt.get();
            model.addAttribute("category", category);
            model.addAttribute("posts", postService.getPostsByCategoryId(id));
            model.addAttribute("totalPosts", postService.countPostsByCategory(id));
            return "public/category-detail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar categoría: " + e.getMessage());
            return "redirect:/categorias";
        }
    }
    
    
}
