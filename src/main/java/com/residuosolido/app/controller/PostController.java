package com.residuosolido.app.controller;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.service.CategoryService;
import com.residuosolido.app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Post Controller - Public read-only endpoints
 * CRUD managed via SQL scripts
 */
@Controller
public class PostController {

    @Autowired
    private PostService postService;
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/posts")
    public String listPosts(
            @RequestParam(value = "category", required = false) String categoryId,
            @RequestParam(value = "q", required = false) String query,
            Model model) {
        try {
            List<Post> posts;
            Category selectedCategory = null;
            
            if (categoryId != null) {
                Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
                if (categoryOpt.isPresent() && categoryOpt.get().isActive()) {
                    selectedCategory = categoryOpt.get();
                    posts = postService.getPostsByCategoryId(categoryId);
                } else {
                    posts = postService.search(query);
                }
            } else {
                posts = postService.search(query);
            }
            
            model.addAttribute("posts", posts);
            model.addAttribute("categories", categoryService.findAllActive());
            model.addAttribute("selectedCategory", selectedCategory);
            model.addAttribute("query", query);
            return "public/posts";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar posts: " + e.getMessage());
            return "public/posts";
        }
    }

    @GetMapping("/posts/{id}")
    public String viewPost(@PathVariable String id, Model model) {
        try {
            Optional<Post> postOpt = postService.getPostById(id);
            if (postOpt.isEmpty() || !postOpt.get().isActive()) {
                return "redirect:/posts";
            }
            
            Post post = postOpt.get();
            model.addAttribute("post", post);
            model.addAttribute("relatedPosts", postService.getRelatedPosts(post, 3));
            return "public/post-detail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar post: " + e.getMessage());
            return "redirect:/posts";
        }
    }

    @GetMapping("/posts/category/{categoryId}")
    public String postsByCategory(@PathVariable String categoryId, Model model) {
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

    @GetMapping("/api/posts/recent")
    @ResponseBody
    public List<Post> getRecentPosts(@RequestParam(defaultValue = "3") int limit) {
        return postService.getRecentPosts(limit);
    }
}
