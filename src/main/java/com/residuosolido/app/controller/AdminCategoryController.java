package com.residuosolido.app.controller;

import com.residuosolido.app.model.Category;
import com.residuosolido.app.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Autowired
    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories";
    }

    @PostMapping("/create")
    public String createCategory(@RequestParam String name) {
        try {
            categoryService.createCategory(name);
            return "redirect:/admin/categories?success=created";
        } catch (Exception e) {
            return "redirect:/admin/categories?error=create";
        }
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id, @RequestParam String name) {
        try {
            Category updated = categoryService.updateCategory(id, name);
            if (updated != null) {
                return "redirect:/admin/categories?success=updated";
            } else {
                return "redirect:/admin/categories?error=notfound";
            }
        } catch (Exception e) {
            return "redirect:/admin/categories?error=update";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        try {
            boolean deleted = categoryService.deleteCategory(id);
            if (deleted) {
                return "redirect:/admin/categories?success=deleted";
            } else {
                return "redirect:/admin/categories?error=notfound";
            }
        } catch (Exception e) {
            return "redirect:/admin/categories?error=delete";
        }
    }
}
