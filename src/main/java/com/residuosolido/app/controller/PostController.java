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
    
    @GetMapping("/about")
    public String about() {
        return "guest/about";
    }

}
