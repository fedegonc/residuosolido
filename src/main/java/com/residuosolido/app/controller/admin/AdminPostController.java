package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.Post;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/posts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(required = false) String action,
                       @RequestParam(required = false) Long id,
                       Model model) {
        List<Post> all = postService.getAllPostsWithCategories();
        model.addAttribute("posts", all);
        model.addAttribute("totalPosts", all != null ? all.size() : 0);

        if ("edit".equals(action) && id != null) {
            Optional<Post> post = postService.getPostById(id);
            post.ifPresent(p -> model.addAttribute("post", p));
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", true);
            return "admin/posts";
        }

        if ("new".equals(action)) {
            model.addAttribute("post", new Post());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", false);
            return "admin/posts";
        }

        model.addAttribute("viewType", "list");
        return "admin/posts";
    }

    @PostMapping
    public String save(@ModelAttribute Post post,
                       RedirectAttributes ra) {
        try {
            if (post.getId() != null) {
                // Editar post existente
                postService.getPostById(post.getId()).ifPresent(existing -> {
                    existing.setTitle(post.getTitle());
                    existing.setContent(post.getContent());
                    existing.setImageUrl(post.getImageUrl());
                    existing.setCategory(post.getCategory());
                    existing.setSourceUrl(post.getSourceUrl());
                    existing.setSourceName(post.getSourceName());
                    postService.save(existing);
                });
                ra.addFlashAttribute("successMessage", "Post actualizado correctamente");
            } else {
                // Crear nuevo post
                postService.save(post);
                ra.addFlashAttribute("successMessage", "Post creado correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            postService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Post eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }
}
