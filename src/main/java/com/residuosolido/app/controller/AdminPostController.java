package com.residuosolido.app.controller;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.repository.CategoryRepository;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.ConfigService;
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
    private final CategoryRepository categoryRepository;
    private final ConfigService configService;

    @Autowired
    public AdminPostController(PostService postService, CategoryRepository categoryRepository, ConfigService configService) {
        this.postService = postService;
        this.categoryRepository = categoryRepository;
        this.configService = configService;
    }

    @GetMapping
    public String listPosts(Model model) {
        model.addAttribute("posts", postService.getAllPosts());
        model.addAttribute("categories", categoryRepository.findAll());
        return "admin/posts";
    }

    @PostMapping
    public String createPost(@RequestParam String title, @RequestParam String content,
                              @RequestParam String imageUrl, @RequestParam Long categoryId) {
        postService.createPost(title, content, imageUrl, categoryId);
        return "redirect:/admin/posts";
    }

    @GetMapping("/{id}/edit")
    public String editPostForm(@PathVariable Long id, Model model) {
        Optional<Post> post = postService.getPostById(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            model.addAttribute("categories", categoryRepository.findAll());
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
        model.addAttribute("heroImages", configService.getAllHeroImages());
        model.addAttribute("activeImage", configService.getHeroImageUrl());
        return "admin/config";
    }
    
    @PostMapping("/config")
    public String updateSiteConfig(@RequestParam("heroImage") MultipartFile heroImageFile) {
        try {
            if (!heroImageFile.isEmpty()) {
                configService.saveHeroImage(heroImageFile);
            }
            return "redirect:/admin/posts/config?success=Imagen subida";
        } catch (Exception e) {
            return "redirect:/admin/posts/config?error=Error al subir imagen";
        }
    }
    
    @PostMapping("/config/activate/{id}")
    public String activateImage(@PathVariable Long id) {
        configService.setActiveImage(id);
        return "redirect:/admin/posts/config?success=Imagen activada";
    }
    
    @PostMapping("/config/delete/{id}")
    public String deleteImage(@PathVariable Long id) {
        configService.deleteHeroImage(id);
        return "redirect:/admin/posts/config?success=Imagen eliminada";
    }
}
