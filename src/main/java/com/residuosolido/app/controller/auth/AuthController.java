package com.residuosolido.app.controller.auth;

import com.residuosolido.app.config.LoginSuccessHandler;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.service.PasswordResetService;
import com.residuosolido.app.service.AuthService;
import com.residuosolido.app.service.PostService;
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

import java.util.Map;
import java.util.List;

import java.util.stream.Collectors;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final PasswordEncoder passwordEncoder;
    private final LoginSuccessHandler successHandler;
    private final PasswordResetService passwordResetService;
    private final AuthService authService;
    private final PostService postService;

    public AuthController(PasswordEncoder passwordEncoder,
                         LoginSuccessHandler successHandler,
                         PasswordResetService passwordResetService,
                         AuthService authService,
                         PostService postService) {
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
        this.passwordResetService = passwordResetService;
        this.authService = authService;
        this.postService = postService;
    }

    @GetMapping({"/", "/index"})
    public String index(
        @AuthenticationPrincipal UserDetails userDetails,
        HttpServletRequest request,
        HttpServletResponse response,
        Model model) throws Exception {

        log.info("[INDEX] GET / - userAuthenticated={} principal={} uri={}", userDetails != null, (userDetails != null ? userDetails.getUsername() : "anonymous"), request.getRequestURI());
        if (userDetails != null) {
            log.info("[INDEX] Authenticated user, delegating to successHandler by role");
            successHandler.redirectByRole(request, response, userDetails);
            return null;
        }

        Map<String, Object> indexData = authService.getIndexData();
        try {
            Object categories = indexData.get("categories");
            Object posts = indexData.get("posts");
            Object orgs = indexData.get("organizations");
            Object hero = indexData.get("heroImage");
            int categoriesCount = (categories instanceof java.util.Collection) ? ((java.util.Collection<?>) categories).size() : (categories == null ? 0 : 1);
            int postsCount = (posts instanceof java.util.Collection) ? ((java.util.Collection<?>) posts).size() : (posts == null ? 0 : 1);
            int orgsCount = (orgs instanceof java.util.Collection) ? ((java.util.Collection<?>) orgs).size() : (orgs == null ? 0 : 1);
            boolean hasHero = (hero instanceof String) && !((String) hero).isEmpty();
            log.info("[INDEX] Data loaded -> categories={}, posts={}, orgs={}, heroImage={}", categoriesCount, postsCount, orgsCount, hasHero ? "yes" : "no");
        } catch (Exception e) {
            log.warn("[INDEX] Error inspecting indexData: {}", e.toString());
        }
        model.addAllAttributes(indexData);

        model.addAttribute("posts", postService.getFirst5Posts());

        return "pages/home";
    }


    @GetMapping("/auth/register")
    public String showRegistrationForm(@AuthenticationPrincipal UserDetails userDetails, 
                                     HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     Model model) throws Exception {
        // Si ya está autenticado, redirigir a su dashboard
        if (userDetails != null) {
            log.info("[REGISTER] User already authenticated, redirecting to dashboard");
            successHandler.redirectByRole(request, response, userDetails);
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
    public String showLoginForm(@AuthenticationPrincipal UserDetails userDetails, 
                               HttpServletRequest request, 
                               HttpServletResponse response) throws Exception {
        // Si ya está autenticado, redirigir a su dashboard
        if (userDetails != null) {
            log.info("[LOGIN] User already authenticated, redirecting to dashboard");
            successHandler.redirectByRole(request, response, userDetails);
            return null;
        }
        
        return "auth/login"; 
    }
    
    @GetMapping("/auth/forgot-password")
    public String showForgotPasswordForm(@AuthenticationPrincipal UserDetails userDetails, 
                                        HttpServletRequest request, 
                                        HttpServletResponse response) throws Exception {
        // Si ya está autenticado, redirigir a su dashboard
        if (userDetails != null) {
            log.info("[FORGOT-PASSWORD] User already authenticated, redirecting to dashboard");
            successHandler.redirectByRole(request, response, userDetails);
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

    // Eliminado handler duplicado de /change-language (ver LanguageController)
}