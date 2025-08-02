package com.residuosolido.app.controller;

import com.residuosolido.app.config.LoginSuccessHandler;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.repository.UserRepository;

import com.residuosolido.app.service.PasswordResetService;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.ConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginSuccessHandler successHandler;
    private final PasswordResetService passwordResetService;
    private final PostService postService;
    private final ConfigService configService;
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                         LoginSuccessHandler successHandler,
                         PasswordResetService passwordResetService,
                         PostService postService, ConfigService configService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
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

    return "guest/index";
}

@GetMapping("/init")
public String init(@AuthenticationPrincipal UserDetails userDetails,
                  HttpServletRequest request,
                  HttpServletResponse response) throws Exception {
    if (userDetails == null) return "redirect:/";
    
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
