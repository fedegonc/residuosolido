package com.residuosolido.app.controller;

import com.residuosolido.app.config.CustomAuthenticationSuccessHandler;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.controller.AdminController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationSuccessHandler successHandler;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, CustomAuthenticationSuccessHandler successHandler) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
    }

   

    @GetMapping({"/", "/index", "/login", "/register"})
    public String index(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) throws Exception {

        if (userDetails != null) {
            successHandler.redirectByRole(request, response, userDetails);
            return null;
        }
        
        // Obtener todos los posts del AdminController
        List<Post> allPosts = AdminController.getAllPosts();
        if (!allPosts.isEmpty()) {
            // Mostrar el primer post (o todos si quieres)
            model.addAttribute("post", allPosts.get(0));
            model.addAttribute("posts", allPosts);
        }
        
        return "index";
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
