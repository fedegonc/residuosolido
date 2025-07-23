package com.residuosolido.app.controller;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
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
public class PublicPostController {

    private final PostService postService;
    private final CategoryService categoryService;

    @Autowired
    public PublicPostController(PostService postService, CategoryService categoryService) {
        this.postService = postService;
        this.categoryService = categoryService;
    }

    @GetMapping("/posts")
    public String listPosts(Model model) {
        List<Post> posts = postService.getAllPosts();
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
            
            // Buscar nombre de categoría
            Optional<Category> categoryOpt = categoryService.getAllCategories().stream()
                .filter(cat -> cat.getId().equals(post.getCategoryId()))
                .findFirst();
            String categoryName = categoryOpt.map(Category::getName).orElse("Sin categoría");
            model.addAttribute("categoryName", categoryName);
            
            // Posts relacionados (misma categoría, excluyendo el actual)
            List<Post> relatedPosts = postService.getAllPosts().stream()
                .filter(p -> p.getCategoryId().equals(post.getCategoryId()) && !p.getId().equals(post.getId()))
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("relatedPosts", relatedPosts);
            
            return "posts/detail";
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
            model.addAttribute("categories", categoryService.getCategoriesWithSlugs());
            return "posts/category";
        }
        
        return "redirect:/posts";
    }
}
