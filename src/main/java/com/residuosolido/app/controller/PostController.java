package com.residuosolido.app.controller;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.repository.CategoryRepository;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.util.List;
import java.util.Optional;

@Controller
public class PostController {
    
    private final CategoryRepository categoryRepository;
    private final PostService postService;
    private final CategoryService categoryService;
    
    @Autowired
    public PostController(CategoryRepository categoryRepository, PostService postService, CategoryService categoryService) {
        this.categoryRepository = categoryRepository;
        this.postService = postService;
        this.categoryService = categoryService;
    }
    
    // El método index se eliminó para evitar ambigüedad con AuthController
    
    @GetMapping("/posts")
    public String listPosts(Model model) {
        List<Post> posts = postService.getAllPosts();
        System.out.println("DEBUG PostController: Posts obtenidos: " + (posts != null ? posts.size() : "null"));
        if (posts != null && !posts.isEmpty()) {
            System.out.println("DEBUG PostController: Primer post: " + posts.get(0).getTitle());
        }
        model.addAttribute("posts", posts);
        model.addAttribute("categories", categoryService.getCategoriesWithSlugs());
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
            return "posts/detail";
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
            return "posts/category";
        }
        
        return "redirect:/posts";
    }
    
    @GetMapping("/categories")
    public String viewAllCategories(Model model) {
        model.addAttribute("categories", categoryService.getCategoriesWithSlugs());
        return "categories/list";
    }
    
    @PostMapping("/api/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("No se seleccionó archivo");
            }
            
            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Solo se permiten imágenes");
            }
            
            // Crear directorio si no existe
            Path uploadDir = Paths.get("src/main/resources/static/uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Generar nombre único
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // Guardar archivo
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // Retornar URL relativa
            return ResponseEntity.ok("/uploads/" + filename);
            
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al subir imagen");
        }
    }
    
    @PostMapping("/api/create-post")
    public ResponseEntity<String> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam("categoryId") Long categoryId) {
        
        try {
            postService.createPost(title, content, imageUrl, categoryId);
            return ResponseEntity.ok("Post creado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear post");
        }
    }

}
