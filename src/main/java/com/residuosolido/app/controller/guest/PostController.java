package com.residuosolido.app.controller.guest;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.CategoryService;
import com.residuosolido.app.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
public class PostController {
    
    private final PostService postService;
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;
    
    @Autowired
    public PostController(PostService postService, CategoryService categoryService, CloudinaryService cloudinaryService) {
        this.postService = postService;
        this.categoryService = categoryService;
        this.cloudinaryService = cloudinaryService;
    }
    
    @GetMapping("/posts")
    public String listPosts(Model model) {
        model.addAttribute("posts", postService.getAllPostsWithCategories());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "posts/list";
    }

    @GetMapping("/posts/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Optional<Post> postOpt = postService.getPostById(id);
        
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            model.addAttribute("post", post);
            model.addAttribute("categoryName", categoryService.getCategoryNameById(post.getCategoryId()));
            model.addAttribute("relatedPosts", postService.getRelatedPostsById(post.getId(), post.getCategoryId(), 3));
            // Simplificado: reutilizamos la lista para mostrar detalles arriba si el template lo soporta
            // o puedes crear un template posts/detail.html más adelante
            model.addAttribute("posts", List.of(post));
            model.addAttribute("categories", categoryService.getAllCategories());
            return "posts/list";
        }
        
        return "redirect:/posts";
    }
    
    @GetMapping("/posts/category/{categorySlug}")
    public String viewPostsByCategory(@PathVariable String categorySlug, Model model) {
        Optional<Category> categoryOpt = categoryService.findBySlug(categorySlug);
            
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            model.addAttribute("posts", postService.getPostsByCategoryId(category.getId()));
            model.addAttribute("category", category);
            model.addAttribute("categories", categoryService.getCategoriesWithSlugs());
            return "posts/list";
        }
        
        return "redirect:/posts";
    }
    
    @GetMapping("/categories")
    public String viewAllCategories(Model model) {
        model.addAttribute("categories", categoryService.getCategoriesWithSlugs());
        return "categories/public";
    }
    
    @GetMapping("/about")
    public String about() {
        // Simplificado: reutilizamos el index público
        return "public/index";
    }
    
    @PostMapping("/posts/create")
    public String createPost(@RequestParam String title,
                           @RequestParam String content,
                           @RequestParam Long categoryId,
                           @RequestParam("image") MultipartFile imageFile) {
        try {
            String imageUrl = "";
            if (!imageFile.isEmpty()) {
                imageUrl = cloudinaryService.uploadFile(imageFile);
            }
            postService.createPost(title, content, imageUrl, categoryId);
            return "redirect:/?success=Post creado exitosamente";
        } catch (Exception e) {
            return "redirect:/?error=Error al crear post: " + e.getMessage();
        }
    }

}
