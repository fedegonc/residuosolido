package com.residuosolido.app.controller;

import com.residuosolido.app.config.LoginSuccessHandler;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.PasswordResetRequestService;
import com.residuosolido.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
}