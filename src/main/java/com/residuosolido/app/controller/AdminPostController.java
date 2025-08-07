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
                              @RequestParam(required = false) String sourceName,
                              @RequestParam(required = false) String sourceUrl,
                              @RequestParam Long categoryId) {
        // Usar el servicio para manejar la lógica de negocio
        postService.createPostWithImage(title, content, imageUrl, imageFile, categoryId, sourceUrl, sourceName);
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
                             @RequestParam(required = false) String imageUrl,
                             @RequestParam(required = false) MultipartFile imageFile,
                             @RequestParam Long categoryId) {
        // Usar el servicio para manejar la lógica de negocio
        postService.updatePostWithImage(id, title, content, imageUrl, imageFile, categoryId);
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
