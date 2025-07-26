package com.residuosolido.app.controller;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.SiteConfig;
import com.residuosolido.app.repository.CategoryRepository;
import com.residuosolido.app.repository.SiteConfigRepository;
import com.residuosolido.app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin/posts-legacy")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {

    private final PostService postService;
    private final CategoryRepository categoryRepository;
    private final SiteConfigRepository siteConfigRepository;

    @Autowired
    public AdminPostController(PostService postService, CategoryRepository categoryRepository, SiteConfigRepository siteConfigRepository) {
        this.postService = postService;
        this.categoryRepository = categoryRepository;
        this.siteConfigRepository = siteConfigRepository;
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
        String heroImage = siteConfigRepository.findByConfigKey("hero_image")
                .map(SiteConfig::getConfigValue)
                .orElse("");
        model.addAttribute("heroImage", heroImage);
        return "admin/config";
    }
    
    @PostMapping("/config")
    public String updateSiteConfig(@RequestParam String heroImage) {
        SiteConfig config = siteConfigRepository.findByConfigKey("hero_image")
                .orElse(new SiteConfig("hero_image", ""));
        config.setConfigValue(heroImage);
        siteConfigRepository.save(config);
        return "redirect:/admin/posts/config";
    }
}
