package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.service.CategoryService;
import com.residuosolido.app.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PostService postService;

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/categories")
    public String listCategories(@RequestParam(required = false) String action,
                                @RequestParam(required = false) Long id,
                                Model model) {
        List<Category> all = categoryService.findAll();
        model.addAttribute("categories", all);
        model.addAttribute("totalCategories", all != null ? all.size() : 0);

        if ("edit".equals(action) && id != null) {
            Optional<Category> category = categoryService.getCategoryById(id);
            category.ifPresent(c -> model.addAttribute("category", c));
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", true);
            return "admin/categories";
        }

        if ("new".equals(action)) {
            model.addAttribute("category", new Category());
            model.addAttribute("viewType", "form");
            model.addAttribute("isEdit", false);
            return "admin/categories";
        }

        model.addAttribute("viewType", "list");
        return "admin/categories";
    }

    @PostMapping("/admin/categories")
    public String saveCategory(@ModelAttribute Category category,
                              RedirectAttributes ra) {
        try {
            if (category.getId() != null) {
                categoryService.getCategoryById(category.getId()).ifPresent(existing -> {
                    existing.setName(category.getName());
                    existing.setDescription(category.getDescription());
                    existing.setImageUrl(category.getImageUrl());
                    existing.setDisplayOrder(category.getDisplayOrder());
                    existing.setActive(category.getActive());
                    categoryService.save(existing);
                });
                ra.addFlashAttribute("successMessage", "Categoría actualizada correctamente");
            } else {
                if (category.getActive() == null) category.setActive(true);
                categoryService.save(category);
                ra.addFlashAttribute("successMessage", "Categoría creada correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Categoría eliminada correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/posts")
    public String listPosts(@RequestParam(required = false) String action,
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

    @PostMapping("/admin/posts")
    public String savePost(@ModelAttribute Post post,
                          RedirectAttributes ra) {
        try {
            if (post.getId() != null) {
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
                postService.save(post);
                ra.addFlashAttribute("successMessage", "Post creado correctamente");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    @PostMapping("/admin/posts/delete/{id}")
    public String deletePost(@PathVariable Long id, RedirectAttributes ra) {
        try {
            postService.deleteById(id);
            ra.addFlashAttribute("successMessage", "Post eliminado correctamente");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error al eliminar: " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    @GetMapping("/admin/documentation")
    public String documentation() {
        return "admin/documentation";
    }
}
