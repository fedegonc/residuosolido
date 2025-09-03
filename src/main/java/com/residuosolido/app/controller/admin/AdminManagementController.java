package com.residuosolido.app.controller.admin;

import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import com.residuosolido.app.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminManagementController {
    @Autowired
    private UserService userService;
    @Autowired
    private FeedbackService feedbackService;

    // Usuarios CRUD básico
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }
    // ...otros métodos de gestión de usuarios, feedback, requests, settings
}
