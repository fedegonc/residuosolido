package com.residuosolido.app.controller.admin;

import com.residuosolido.app.service.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.findAll().size());
        model.addAttribute("totalPosts", postService.getAllPosts().size());
        model.addAttribute("totalCategories", categoryService.getAllCategories().size());
        model.addAttribute("totalFeedbacks", feedbackService.findAll().size());
        return "dashboard";
    }
}
