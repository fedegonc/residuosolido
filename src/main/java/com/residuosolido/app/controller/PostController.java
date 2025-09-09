package com.residuosolido.app.controller;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Post Controller - Maneja posts por rol
 * Endpoints:
 * - /admin/posts → ROLE_ADMIN (CRUD completo)
 * - /posts → Público (solo lectura)
 */
@Controller
public class PostController {

    @Autowired
    private PostService postService;
    
    @Autowired
    private CategoryService categoryService;

    // ========== ADMIN ENDPOINTS ==========
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/posts")
    public String adminPosts(@RequestParam(value = "action", required = false) String action,
                            @RequestParam(value = "id", required = false) Long id,
                            Model model) {
        
        String viewType = "list";
        Post post = new Post();
        
        if ("new".equals(action)) {
            viewType = "form";
            // post ya está inicializado
        } else if ("edit".equals(action) && id != null) {
            viewType = "form";
            post = postService.getPostById(id).orElse(new Post());
        } else if ("view".equals(action) && id != null) {
            viewType = "view";
            post = postService.getPostById(id).orElse(new Post());
        }
        
        model.addAttribute("posts", postService.findAll());
        model.addAttribute("post", post);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("viewType", viewType);
        model.addAttribute("totalPosts", postService.count());
        
        return "admin/posts";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/posts")
    public String adminSavePost(@RequestParam("action") String action,
                               @ModelAttribute Post post,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                               RedirectAttributes redirectAttributes) {
        try {
            if ("delete".equals(action)) {
                postService.deleteById(post.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Post eliminado correctamente");
            } else {
                // Manejar imagen si se subió
                if (imageFile != null && !imageFile.isEmpty()) {
                    // Aquí iría la lógica de subida de imagen
                    // post.setImageUrl(cloudinaryService.uploadFile(imageFile));
                }
                
                postService.save(post);
                String message = post.getId() == null ? "Post creado correctamente" : "Post actualizado correctamente";
                redirectAttributes.addFlashAttribute("successMessage", message);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar post: " + e.getMessage());
        }
        
        return "redirect:/admin/posts";
    }

    // ========== PUBLIC ENDPOINTS ==========
    @GetMapping("/posts")
    public String publicPosts(Model model) {
        List<Post> posts = postService.findAll();
        model.addAttribute("posts", posts);
        return "public/posts";
    }

    @GetMapping("/posts/{id}")
    public String publicPostDetail(@PathVariable Long id, Model model) {
        Post post = postService.getPostById(id).orElse(null);
        if (post == null) {
            return "redirect:/posts";
        }
        
        model.addAttribute("post", post);
        
        // Obtener posts relacionados (misma categoría, excluyendo el actual)
        List<Post> relatedPosts = postService.getRelatedPosts(post, 3);
        model.addAttribute("relatedPosts", relatedPosts);
        
        return "public/post-detail";
    }

    


    @GetMapping("/posts/recent")
    public String publicRecentPosts(Model model) {
        List<Post> recentPosts = postService.getRecentPosts(5);
        model.addAttribute("posts", recentPosts);
        return "public/recent-posts";
    }

    @GetMapping("/posts/featured")
    public String publicFeaturedPosts(Model model) {
        List<Post> featuredPosts = postService.getFeaturedPosts();
        model.addAttribute("posts", featuredPosts);
        return "public/featured-posts";
    }
}
