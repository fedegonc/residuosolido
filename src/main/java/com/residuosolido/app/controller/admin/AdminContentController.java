package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.*;
import com.residuosolido.app.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminContentController {

    private final PostService postService;
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;

    public AdminContentController(PostService postService, CategoryService categoryService, 
                                CloudinaryService cloudinaryService) {
        this.postService = postService;
        this.categoryService = categoryService;
        this.cloudinaryService = cloudinaryService;
    }

    // POSTS MANAGEMENT
    @GetMapping("/posts")
    public String listPosts(Model model) {
        model.addAttribute("posts", postService.getAllPostsWithCategories());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/posts";
    }

    @GetMapping("/posts/create")
    public String createPostForm(Model model) {
        model.addAttribute("post", new Post());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/post-form";
    }

    @PostMapping("/posts")
    public String savePost(@ModelAttribute Post post, @RequestParam("imageFile") MultipartFile file,
                          RedirectAttributes redirectAttributes) {
        try {
            if (!file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(file);
                post.setImageUrl(imageUrl);
            }
            postService.createPost(post.getTitle(), post.getContent(), post.getImageUrl(), post.getCategory().getId());
            redirectAttributes.addFlashAttribute("success", "Post guardado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar post: " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    @GetMapping("/posts/{id}/edit")
    public String editPostForm(@PathVariable Long id, Model model) {
        Optional<Post> post = postService.getPostById(id);
        if (post.isPresent()) {
            model.addAttribute("post", post.get());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/post-form";
        }
        return "redirect:/admin/posts";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            postService.deletePost(id);
            redirectAttributes.addFlashAttribute("success", "Post eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar post: " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    // CATEGORIES MANAGEMENT
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @GetMapping("/categories/create")
    public String createCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(category.getName());
            redirectAttributes.addFlashAttribute("success", "Categoría guardada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar categoría: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            return "admin/category-form";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Categoría eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar categoría: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
