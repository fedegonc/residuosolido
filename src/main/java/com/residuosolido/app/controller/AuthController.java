package com.residuosolido.app.controller;

import com.residuosolido.app.config.LoginSuccessHandler;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.service.PasswordResetService;
import com.residuosolido.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class AuthController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginSuccessHandler successHandler;
    private final PasswordResetService passwordResetService;
    private final AuthService authService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                         LoginSuccessHandler successHandler,
                         PasswordResetService passwordResetService,
                         AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
        this.passwordResetService = passwordResetService;
        this.authService = authService;
    }

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

        Map<String, Object> indexData = authService.getIndexData();
        model.addAllAttributes(indexData);

        return "guest/index";
    }

    @GetMapping("/auth/register")
    public String showRegistrationForm(Model model) {
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