package com.residuosolido.app.controller;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.repository.CategoryRepository;
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
    
    @Autowired
    public PostController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    // El método index se eliminó para evitar ambigüedad con AuthController
    
    @GetMapping("/posts")
    public String listPosts(Model model) {
        // Obtener todos los posts del AdminController
        List<Post> posts = com.residuosolido.app.controller.AdminController.getAllPosts();
        model.addAttribute("posts", posts);
        model.addAttribute("categories", categoryRepository.findAll());
        return "posts/list";
    }
    
    @GetMapping("/posts/{id}")
    public String viewPost(@PathVariable Long id, Model model) {
        // Buscar el post por ID
        Optional<Post> postOpt = com.residuosolido.app.controller.AdminController.getAllPosts().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
        
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            model.addAttribute("post", post);
            
            // Obtener el nombre de la categoría
            Optional<Category> categoryOpt = categoryRepository.findById(post.getCategoryId());
            String categoryName = categoryOpt.map(Category::getName).orElse("Sin categoría");
            model.addAttribute("categoryName", categoryName);
            
            return "posts/view";
        }
        
        return "redirect:/posts";
    }
}
