package com.residuosolido.app.controller;

import com.residuosolido.app.config.CustomAuthenticationSuccessHandler;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.service.DashboardService;
import com.residuosolido.app.service.PostService;
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

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                         CustomAuthenticationSuccessHandler successHandler,
                         DashboardService dashboardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
        this.dashboardService = dashboardService;
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

    return "guest/index";
}

@GetMapping("/invitados")
public String invitados(Model model) {
    java.util.Map<String, Object> pageData = dashboardService.getPublicPageData();
    model.addAllAttributes(pageData);
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
    
    @GetMapping("/auth/logout")
    public String showLogoutPage() {
        return "index";
    }
    
    
}
