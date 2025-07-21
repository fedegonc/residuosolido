package com.residuosolido.app.controller;

import com.residuosolido.app.model.Post;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class InitController {
    
    @GetMapping("/init")
    public String init(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/invitados";
        
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN" -> "redirect:/admin/dashboard";
            case "ROLE_ORGANIZATION" -> "redirect:/org/dashboard";
            default -> "redirect:/users/dashboard";
        };
    }
    
    @GetMapping("/invitados")
    public String invitados(Model model) {
        List<Post> posts = AdminController.getAllPosts();
        if (posts == null) posts = new ArrayList<>();
        model.addAttribute("posts", posts);
        return "index";
    }
}
