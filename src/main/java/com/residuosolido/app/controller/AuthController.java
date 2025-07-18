package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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


}
