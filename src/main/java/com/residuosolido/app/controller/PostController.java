package com.residuosolido.app.controller;

import com.residuosolido.app.model.Post;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PostController {
    
    @GetMapping("/posts")
    public String listPosts(Model model) {
        // Datos hardcodeados básicos para que funcione el frontend
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Campaña de Reciclaje 2025");
        post.setImageUrl("https://via.placeholder.com/400x200");
        
        model.addAttribute("post", post);
        return "posts/list";
    }
    
}
