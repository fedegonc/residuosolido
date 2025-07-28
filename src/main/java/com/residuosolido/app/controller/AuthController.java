package com.residuosolido.app.controller;

import com.residuosolido.app.config.CustomAuthenticationSuccessHandler;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.service.DashboardService;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.PasswordResetService;
import com.residuosolido.app.service.ConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final DashboardService dashboardService;
    private final PasswordResetService passwordResetService;
    private final PostService postService;
    private final ConfigService configService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                         CustomAuthenticationSuccessHandler successHandler,
                         DashboardService dashboardService, PasswordResetService passwordResetService,
                         PostService postService, ConfigService configService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
        this.dashboardService = dashboardService;
        this.passwordResetService = passwordResetService;
        this.postService = postService;
        this.configService = configService;
    }

   

    // Ya no se necesita PostService, se usa dashboardService

    @GetMapping({"/", "/index"})
    public String index(
        @AuthenticationPrincipal UserDetails userDetails,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model) throws Exception {

    if (userDetails != null) {
        successHandler.redirectByRole(request, response, userDetails);
        return null;
    }

    java.util.Map<String, Object> pageData = dashboardService.getPublicPageData();
    model.addAllAttributes(pageData);
    
    // Agregar posts para mostrar en el index
    List<Post> posts = postService.getAllPosts();
    System.out.println("DEBUG: Posts obtenidos: " + (posts != null ? posts.size() : "null"));
    if (posts != null && !posts.isEmpty()) {
        System.out.println("DEBUG: Primer post: " + posts.get(0).getTitle());
    }
    model.addAttribute("posts", posts);
    
    // Agregar organizaciones (usuarios con rol ORGANIZATION)
    List<User> organizations = userRepository.findByRole(Role.ORGANIZATION);
    System.out.println("DEBUG: Organizaciones encontradas: " + (organizations != null ? organizations.size() : "null"));
    model.addAttribute("organizations", organizations);
    
    // Agregar imagen del hero
    model.addAttribute("heroImage", configService.getHeroImageUrl());

    return "guest/index";
}

@GetMapping("/invitados")
public String invitados(Model model) {
    java.util.Map<String, Object> pageData = dashboardService.getPublicPageData();
    model.addAllAttributes(pageData);
    
    // Agregar posts para mostrar en el index
    List<Post> posts = postService.getAllPosts();
    System.out.println("DEBUG: Posts obtenidos: " + (posts != null ? posts.size() : "null"));
    if (posts != null && !posts.isEmpty()) {
        System.out.println("DEBUG: Primer post: " + posts.get(0).getTitle());
    }
    model.addAttribute("posts", posts);
    
    // Agregar organizaciones (usuarios con rol ORGANIZATION)
    List<User> organizations = userRepository.findByRole(Role.ORGANIZATION);
    System.out.println("DEBUG: Organizaciones encontradas: " + (organizations != null ? organizations.size() : "null"));
    model.addAttribute("organizations", organizations);
    
    return "guest/index";
}

@GetMapping("/init")
public String init(@AuthenticationPrincipal UserDetails userDetails,
                  HttpServletRequest request,
                  HttpServletResponse response) throws Exception {
    if (userDetails == null) return "redirect:/invitados";
    
    // Usar el mismo handler que maneja el login exitoso
    successHandler.redirectByRole(request, response, userDetails);
    return null; // La redirección ya fue manejada por el handler
}


    @GetMapping("/auth/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String registerUser(@ModelAttribute User user) {
        
        // Verificar si el usuario ya existe
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "redirect:/auth/register?error=El nombre de usuario ya está en uso";
        }

        // Verificar si el email ya existe
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return "redirect:/auth/register?error=El email ya está registrado";
        }

        // Encriptar la contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Establecer el rol por defecto
        user.setRole(Role.USER);
        
        // Establecer idioma predeterminado
        user.setPreferredLanguage("es");
        
        // Guardar el usuario
        userRepository.save(user);

        return "redirect:/auth/login?success=Registro exitoso";
    }
    @GetMapping("/auth/login")
    public String showLoginForm() {
        return "auth/login"; 
    }
    
    @GetMapping("/auth/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }
    
    @PostMapping("/auth/forgot-password")
    public String processForgotPassword(@RequestParam String maskedEmail, 
                                       @RequestParam String lastKnownPassword) {
        passwordResetService.createResetRequest(maskedEmail, lastKnownPassword);
        return "redirect:/auth/login?info=Solicitud enviada al administrador";
    }
    
    
}
