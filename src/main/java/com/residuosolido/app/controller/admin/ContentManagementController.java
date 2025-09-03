package com.residuosolido.app.controller.admin;

import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/content")
@PreAuthorize("hasRole('ADMIN')")
public class ContentManagementController {
    @Autowired
    private PostService postService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/posts")
    public String listPosts(Model model) {
        model.addAttribute("posts", postService.findAllWithCategories());
        return "admin/posts";
    }
    // ...otros métodos para categorías
}
