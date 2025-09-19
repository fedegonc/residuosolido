package com.residuosolido.app.controller;

import com.residuosolido.app.config.LoginSuccessHandler;
import com.residuosolido.app.model.Category;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.service.CategoryService;
import com.residuosolido.app.service.PasswordResetRequestService;
import com.residuosolido.app.service.AuthService;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.ConfigService;
import com.residuosolido.app.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final PasswordEncoder passwordEncoder;
    private final LoginSuccessHandler successHandler;
    private final PasswordResetRequestService passwordResetService;
    private final AuthService authService;

    public AuthController(PasswordEncoder passwordEncoder,
                         LoginSuccessHandler successHandler,
                         PasswordResetRequestService passwordResetService,
                         AuthService authService) {
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
        this.passwordResetService = passwordResetService;
        this.authService = authService;
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
    public String registerUser(@ModelAttribute User user, Model model) {
        String validationError = authService.validateUserRegistration(user);
        if (validationError != null) {
            model.addAttribute("error", validationError);
            return "auth/register";
        }
        
        authService.registerUser(user);
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
    
    @GetMapping("/auth/forgot-password")
    public String showForgotPasswordForm(@AuthenticationPrincipal UserDetails userDetails, 
                                        HttpServletRequest request, 
                                        HttpServletResponse response) throws Exception {
        if (userDetails != null) {
            log.info("[FORGOT-PASSWORD] User already authenticated, redirecting to dashboard");
            successHandler.redirectToDashboard(request, response, userDetails);
            return null;
        }
        
        return "auth/forgot-password";
    }
    
    @PostMapping("/auth/forgot-password")
    public String processForgotPassword(@RequestParam String maskedEmail, 
                                       @RequestParam String lastKnownPassword) {
        passwordResetService.createResetRequest(maskedEmail, lastKnownPassword);
        return "redirect:/auth/login?info=Solicitud enviada al administrador";
    }

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private ConfigService configService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping({"/", "/index"})
    public String rootOrIndex(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !isAnonymous(auth)) {
            // Usuario autenticado: redirigir a su panel correspondiente
            return "redirect:" + resolvePanel(auth);
        }
        
        // Cargar categorías activas para mostrar en la página de inicio
        List<Category> activeCategories = categoryService.findAllActive();
        model.addAttribute("categories", activeCategories);
        
        // Cargar organizaciones para mostrar en la página de inicio
        List<User> organizations = userService.findByRole(Role.ORGANIZATION);
        model.addAttribute("organizations", organizations);
        
        // Cargar imagen del hero desde configuración
        String heroImageUrl = configService.getHeroImageUrl();
        model.addAttribute("heroImageUrl", heroImageUrl);
        
        // Cargar posts recientes para mostrar en la página de inicio
        List<Post> recentPosts = postService.getRecentPosts(3);
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


    private boolean isAnonymous(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ANONYMOUS"));
    }

    private String resolvePanel(Authentication auth) {
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        if (roles.contains("ROLE_ADMIN")) {
            return "/admin/dashboard";
        }
        if (roles.contains("ROLE_ORGANIZATION")) {
            return "/org/dashboard";
        }
        return "/user/dashboard";
    }

}
