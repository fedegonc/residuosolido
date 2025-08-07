package com.residuosolido.app.controller;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.service.CategoryService;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.ConfigService;
import com.residuosolido.app.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
@RequestMapping("/admin/posts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {

    private final PostService postService;
    private final CategoryService categoryService;
    private final ConfigService configService;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public AdminPostController(PostService postService, CategoryService categoryService, ConfigService configService, CloudinaryService cloudinaryService) {
        this.postService = postService;
        this.categoryService = categoryService;
        this.configService = configService;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public String listPosts(Model model) {
        model.addAttribute("posts", postService.getAllPostsWithCategories());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/posts";
    }

    @PostMapping
    public String createPost(@RequestParam String title, @RequestParam String content,
                              @RequestParam(required = false) String imageUrl, 
                              @RequestParam(required = false) MultipartFile imageFile,
                              @RequestParam Long categoryId) {
        String finalImageUrl = imageUrl;
        
        // Si se subió un archivo, usar Cloudinary
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                finalImageUrl = cloudinaryService.uploadFile(imageFile);
            } catch (Exception e) {
                // Si falla Cloudinary, usar URL manual si está disponible
                System.err.println("Error subiendo a Cloudinary: " + e.getMessage());
            }
        }
        
        postService.createPost(title, content, finalImageUrl, categoryId);
        return "redirect:/admin/posts";
    }

    @GetMapping("/{id}/edit")
    public String editPostForm(@PathVariable Long id, Model model) {
        Optional<Post> post = postService.getPostById(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/edit-post";
        }
        return "redirect:/admin/posts";
    }

    @PostMapping("/{id}/update")
    public String updatePost(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String content,
                             @RequestParam String imageUrl,
                             @RequestParam Long categoryId) {
        postService.updatePost(id, title, content, imageUrl, categoryId);
        return "redirect:/admin/posts";
    }

    @PostMapping("/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/admin/posts";
    }
    
    @GetMapping("/config")
    public String siteConfig(Model model) {
        model.addAttribute("activeImage", configService.getHeroImageUrl());
        return "admin/config";
    }


}
