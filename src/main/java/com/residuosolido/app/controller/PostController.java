package com.residuosolido.app.controller;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.service.CategoryService;
import com.residuosolido.app.service.CloudinaryService;
import com.residuosolido.app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Post Controller - Controlador unificado para gestión de posts
 * 
 * Estructura:
 * - /posts - Endpoint público (solo lectura)
 * - /posts/{id} - Detalle de post
 * - /posts/category/{id} - Posts por categoría
 * - /posts/recent - Posts recientes
 * - /posts/featured - Posts destacados
 * - /admin/posts - Admin (CRUD completo)
 */
@Controller
public class PostController {

    @Autowired
    private PostService postService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired(required = false)
    private CloudinaryService cloudinaryService;

    // ========== MÉTODOS COMUNES ==========
    
    /**
     * Prepara modelo común para todas las vistas de posts
     */
    private void preparePostModel(Model model, List<Post> posts, String viewType) {
        model.addAttribute("posts", posts);
        model.addAttribute("totalPosts", posts.size());
        model.addAttribute("viewType", viewType);
    }
    
    /**
     * Maneja errores comunes en operaciones de posts
     */
    private void handlePostError(Exception e, RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute("errorMessage", message + ": " + e.getMessage());
    }

    // ========== ADMIN ENDPOINTS ==========
    
    /**
     * Gestiona posts (admin)
     * Soporta vistas: list, form, view
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/posts")
    public String adminPosts(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        
        // Valor por defecto: lista
        String viewType = "list";
        Post post = new Post();
        
        // Determinar tipo de vista según acción
        if ("new".equals(action)) {
            viewType = "form";
            // post ya está inicializado como nuevo
        } else if ("edit".equals(action) && id != null) {
            viewType = "form";
            post = postService.getPostById(id).orElse(new Post());
        } else if ("view".equals(action) && id != null) {
            viewType = "view";
            post = postService.getPostById(id).orElse(new Post());
        }
        
        // Preparar modelo común
        List<Post> allPosts = postService.findAll();
        preparePostModel(model, allPosts, viewType);
        model.addAttribute("post", post);
        model.addAttribute("categories", categoryService.findAll());
        
        return "admin/posts";
    }

    /**
     * Procesa operaciones CRUD de posts (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/posts")
    public String adminSavePost(
            @RequestParam(value = "action", required = false) String action,
            @ModelAttribute Post post,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        try {
            // Operación según acción
            if ("delete".equals(action)) {
                postService.deleteById(post.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Post eliminado correctamente");
            } else {
                // Crear o actualizar
                boolean isNew = post.getId() == null;
                
                // Manejar imagen si se subió
                if (imageFile != null && !imageFile.isEmpty() && cloudinaryService != null) {
                    String imageUrl = cloudinaryService.uploadFile(imageFile);
                    post.setImageUrl(imageUrl);
                }
                
                // Guardar post
                postService.save(post);
                String message = isNew ? "Post creado correctamente" : "Post actualizado correctamente";
                redirectAttributes.addFlashAttribute("successMessage", message);
            }
        } catch (Exception e) {
            handlePostError(e, redirectAttributes, "Error al procesar post");
        }
        
        return "redirect:/admin/posts";
    }

    /**
     * Elimina un post por ID (admin)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/posts/delete/{id}")
    public String adminDeletePost(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            postService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Post eliminado correctamente");
        } catch (Exception e) {
            handlePostError(e, redirectAttributes, "Error al eliminar post");
        }
        
        return "redirect:/admin/posts";
    }

    // ========== PUBLIC ENDPOINTS ==========
    
    /**
     * Lista todos los posts (público)
     */
    @GetMapping("/posts")
    public String publicPosts(Model model) {
        try {
            List<Post> activePosts = postService.findAllActive();
            model.addAttribute("posts", activePosts);
            model.addAttribute("categories", categoryService.findAllActive());
            return "public/posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar posts: " + e.getMessage());
            return "public/posts";
        }
    }

    /**
     * Muestra detalle de un post (público)
     */
    @GetMapping("/posts/{id}")
    public String publicPostDetail(@PathVariable Long id, Model model) {
        try {
            Optional<Post> postOpt = postService.getPostById(id);
            if (postOpt.isEmpty() || !postOpt.get().isActive()) {
                return "redirect:/posts";
            }
            
            Post post = postOpt.get();
            model.addAttribute("post", post);
            
            // Obtener posts relacionados (misma categoría, excluyendo el actual)
            List<Post> relatedPosts = postService.getRelatedPosts(post, 3);
            model.addAttribute("relatedPosts", relatedPosts);
            
            return "public/post-detail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar post: " + e.getMessage());
            return "redirect:/posts";
        }
    }

    /**
     * Lista posts por categoría (público)
     */
    @GetMapping("/posts/category/{categoryId}")
    public String publicPostsByCategory(@PathVariable Long categoryId, Model model) {
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isEmpty() || !categoryOpt.get().isActive()) {
                return "redirect:/categories";
            }
            
            Category category = categoryOpt.get();
            List<Post> categoryPosts = postService.getPostsByCategoryId(categoryId);
            
            model.addAttribute("category", category);
            model.addAttribute("posts", categoryPosts);
            model.addAttribute("totalPosts", categoryPosts.size());
            
            return "public/category-posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar posts de categoría: " + e.getMessage());
            return "redirect:/categories";
        }
    }

    /**
     * Lista posts recientes (público)
     */
    @GetMapping("/posts/recent")
    public String publicRecentPosts(Model model) {
        try {
            List<Post> recentPosts = postService.getRecentPosts(5);
            model.addAttribute("posts", recentPosts);
            model.addAttribute("title", "Posts Recientes");
            return "public/recent-posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar posts recientes: " + e.getMessage());
            return "redirect:/posts";
        }
    }

    /**
     * Lista posts destacados (público)
     */
    @GetMapping("/posts/featured")
    public String publicFeaturedPosts(Model model) {
        try {
            List<Post> featuredPosts = postService.getFeaturedPosts();
            model.addAttribute("posts", featuredPosts);
            model.addAttribute("title", "Posts Destacados");
            return "public/featured-posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar posts destacados: " + e.getMessage());
            return "redirect:/posts";
        }
    }
    
    /**
     * API para obtener posts recientes
     */
    @GetMapping("/api/posts/recent")
    @ResponseBody
    public List<Post> getRecentPosts(@RequestParam(defaultValue = "3") int limit) {
        return postService.getRecentPosts(limit);
    }
}
