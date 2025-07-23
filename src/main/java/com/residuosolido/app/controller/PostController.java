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
        // Obtener todos los posts del AdminController
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        model.addAttribute("categories", categoryRepository.findAll());
        return "posts/list";
    }
    
    @GetMapping("/posts/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        Optional<Post> postOpt = postService.getPostById(id);
        
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            model.addAttribute("post", post);
            
            Optional<Category> categoryOpt = categoryRepository.findById(post.getCategoryId());
            String categoryName = categoryOpt.map(Category::getName).orElse("Sin categoría");
            model.addAttribute("categoryName", categoryName);
            
            return "posts/view";
        }
        
        return "redirect:/posts";
    }
    
    @GetMapping("/posts/category/{categorySlug}")
    public String viewPostsByCategory(@PathVariable String categorySlug, Model model) {
        Optional<Category> categoryOpt = categoryService.findBySlug(categorySlug);
            
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            List<Post> posts = postService.getAllPosts().stream()
                .filter(post -> post.getCategoryId().equals(category.getId()))
                .collect(java.util.stream.Collectors.toList());
                
            model.addAttribute("posts", posts);
            model.addAttribute("category", category);
            model.addAttribute("categories", categoryRepository.findAll());
            return "posts/category";
        }
        
        return "redirect:/posts";
    }

}
