package com.residuosolido.app.controller;

import com.residuosolido.app.config.LoginSuccessHandler;
import com.residuosolido.app.model.User;
import com.residuosolido.app.service.PasswordResetRequestService;
import com.residuosolido.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping({"/", "/index"})
    public String rootOrIndex(RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !isAnonymous(auth)) {
            String target = resolvePanel(auth);
            redirectAttributes.addFlashAttribute("infoMessage", "Ya estás autenticado. Te redirigimos a tu panel.");
            return "redirect:" + target;
        }
        return "redirect:/auth/login";
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
