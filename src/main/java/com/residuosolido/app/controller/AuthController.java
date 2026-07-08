package com.residuosolido.app.controller;

import com.residuosolido.app.config.LoginSuccessHandler;
import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.service.CategoryService;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final LoginSuccessHandler successHandler;

    public AuthController(LoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @GetMapping("/auth/register")
    public String showRegistrationForm(@AuthenticationPrincipal UserDetails userDetails, 
                                     HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     Model model) throws Exception {
        if (userDetails != null) {
            log.info("[REGISTER] User already authenticated, redirecting to dashboard");
            successHandler.redirectToDashboard(request, response, userDetails);
            return null;
        }
        
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String registerUser(@ModelAttribute User user, 
                               @RequestParam(value = "isOrganization", required = false) String isOrganization,
                               Model model) {
        String validationError = userService.validateUserRegistration(user);
        if (validationError != null) {
            model.addAttribute("error", validationError);
            return "auth/register";
        }
        
        // Registrar usuario con el rol apropiado según el checkbox
        userService.registerUser(user, isOrganization != null);
        return "redirect:/auth/login?success=Registro exitoso. Inicia sesión con tus credenciales.";
    }
    
    @GetMapping("/auth/login")
    public String showLoginPage() {
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/";
    }
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private UserService userService;
    
    @Value("${app.hero.image-url:/images/hero-default.jpg}")
    private String heroImageUrl;
    
    @GetMapping({"/", "/index"})
    public String rootOrIndex(Model model) {
        // Permitir que usuarios autenticados también vean el contenido público del index
        // (categorías, posts, organizaciones) sin redirigir automáticamente a su dashboard
        
        // Cargar categorías activas para mostrar en la página de inicio
        List<Category> activeCategories = categoryService.findAllActive();
        model.addAttribute("categories", activeCategories);
        
        // Cargar organizaciones para mostrar en la página de inicio
        List<User> organizations = userService.findByRole(Role.ORGANIZATION);
        model.addAttribute("organizations", organizations);
        
        model.addAttribute("heroImageUrl", heroImageUrl);
        
        // Cargar posts recientes para mostrar en la página de inicio
        List<Post> recentPosts = postService.getRecentPosts(10);
        List<Map<String, String>> recentNotes = new ArrayList<>();
        
        if (recentPosts.isEmpty()) {
            // Fallback hardcode si no hay posts en la base de datos
            Map<String, String> note1 = new HashMap<>();
            note1.put("title", "Bienvenidos a Residuos Sólidos");
            note1.put("image", "/images/placeholder.jpg");
            note1.put("excerpt", "Descubre cómo podemos ayudar a gestionar los residuos en la región fronteriza de una manera sostenible.");
            note1.put("url", "/about");
            recentNotes.add(note1);
            
            Map<String, String> note2 = new HashMap<>();
            note2.put("title", "Cómo reciclar correctamente");
            note2.put("image", "/images/placeholder.jpg");
            note2.put("excerpt", "Guía práctica para separar y preparar tus residuos reciclables antes de la recolección.");
            note2.put("url", "/about");
            recentNotes.add(note2);
            
            Map<String, String> note3 = new HashMap<>();
            note3.put("title", "Organizaciones participantes");
            note3.put("image", "/images/placeholder.jpg");
            note3.put("excerpt", "Conoce las organizaciones que colaboran en la recolección y reciclaje en Rivera y Sant'Ana do Livramento.");
            note3.put("url", "/about");
            recentNotes.add(note3);
        } else {
            // Convertir posts reales a mapas
            for (Post post : recentPosts) {
                Map<String, String> note = new HashMap<>();
                note.put("title", post.getTitle());
                note.put("image", post.getImageUrl() != null ? post.getImageUrl() : "/images/placeholder.jpg");
                String excerpt = post.getContent() != null && post.getContent().length() > 100 
                    ? post.getContent().substring(0, 100) + "..." 
                    : post.getContent() != null ? post.getContent() : "";
                note.put("excerpt", excerpt);
                note.put("url", "/posts/" + post.getId());
                recentNotes.add(note);
            }
        }
        
        model.addAttribute("recentNotes", recentNotes);
        
        // Usuario no autenticado: mostrar la página de inicio (index.html)
        return "index";
    }


}
