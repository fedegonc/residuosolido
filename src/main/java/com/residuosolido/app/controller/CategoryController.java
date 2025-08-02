package com.residuosolido.app.controller;

import com.residuosolido.app.service.PostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CategoryController {

    @Autowired
    private PostService postService;
    
    @GetMapping("/category/{categoryName}")
    public String showCategoryPosts(@PathVariable String categoryName, Model model) {
        // Simplificado sin WasteSectionService
        model.addAttribute("posts", java.util.Collections.emptyList());
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("categoryDescription", "Categoría: " + categoryName);
        
        return "guest/category";
    }
}
