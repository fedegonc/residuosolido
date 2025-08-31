package com.residuosolido.app.controller.admin;

import com.residuosolido.app.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final UserService userService;
    private final PostService postService;
    private final CategoryService categoryService;
    private final FeedbackService feedbackService;

    public AdminDashboardController(UserService userService, PostService postService,
                                  CategoryService categoryService, FeedbackService feedbackService) {
        this.userService = userService;
        this.postService = postService;
        this.categoryService = categoryService;
        this.feedbackService = feedbackService;
    }

    // Web Template Endpoint
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Estadísticas principales
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalPosts", postService.getAllPosts().size());
        model.addAttribute("totalCategories", categoryService.getAllCategories().size());
        model.addAttribute("totalFeedbacks", feedbackService.findAll().size());

        // Estadísticas adicionales para admin
        model.addAttribute("recentUsers", userService.findAll().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5)
            .toList());
        model.addAttribute("recentPosts", postService.getAllPosts().stream()
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .limit(5)
            .toList());

        return "admin/dashboard";
    }

    // Root admin redirect
    @GetMapping
    public String dashboard() {
        return "redirect:/admin/dashboard";
    }
}
