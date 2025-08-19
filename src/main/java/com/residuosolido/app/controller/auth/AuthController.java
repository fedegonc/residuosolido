package com.residuosolido.app.controller.auth;

import com.residuosolido.app.config.LoginSuccessHandler;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.service.PasswordResetService;
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
import org.springframework.web.servlet.LocaleResolver;

import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginSuccessHandler successHandler;
    private final PasswordResetService passwordResetService;
    private final AuthService authService;
    private final LocaleResolver localeResolver;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                         LoginSuccessHandler successHandler,
                         PasswordResetService passwordResetService,
                         AuthService authService,
                         LocaleResolver localeResolver) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
        this.passwordResetService = passwordResetService;
        this.authService = authService;
        this.localeResolver = localeResolver;
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

        return "guest/index";
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
        return "redirect:/auth/login?success=Registro exitoso";
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

    // Endpoint para cambio de idioma
    @GetMapping("/change-language")
    public String changeLanguage(
            @RequestParam("lang") String language,
            @RequestParam(value = "referer", required = false) String explicitReferer,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        log.info("[LANGUAGE] Solicitud de cambio de idioma recibida: lang={}, referer={}", language, explicitReferer);
        
        Locale locale;
        switch (language.toLowerCase()) {
            case "es":
                locale = new Locale("es");
                log.info("[LANGUAGE] Cambiando a español");
                break;
            case "pt":
            default:
                locale = new Locale("pt");
                log.info("[LANGUAGE] Cambiando a portugués");
                break;
        }
        
        // Obtener locale actual antes del cambio
        Locale currentLocale = localeResolver.resolveLocale(request);
        log.info("[LANGUAGE] Locale actual antes del cambio: {}", currentLocale);
        
        // Aplicar el cambio
        localeResolver.setLocale(request, response, locale);
        log.info("[LANGUAGE] Locale establecido a: {}", locale);
        
        // Verificar si el cambio se aplicó correctamente
        Locale afterChangeLocale = localeResolver.resolveLocale(request);
        log.info("[LANGUAGE] Locale después del cambio: {}", afterChangeLocale);
        
        // Determinar URL de redirección
        String redirectUrl;
        
        // Primero intentar usar el referer explícito si existe
        if (explicitReferer != null && !explicitReferer.isEmpty()) {
            redirectUrl = explicitReferer;
            log.info("[LANGUAGE] Usando referer explícito para redirección: {}", redirectUrl);
        } else {
            // Si no hay referer explícito, usar el header Referer
            String refererHeader = request.getHeader("Referer");
            log.info("[LANGUAGE] Header Referer: {}", refererHeader);
            
            if (refererHeader != null && !refererHeader.isEmpty()) {
                redirectUrl = refererHeader;
                log.info("[LANGUAGE] Usando header Referer para redirección: {}", redirectUrl);
            } else {
                // Si no hay ningún referer, redirigir a la página principal
                redirectUrl = "/";
                log.info("[LANGUAGE] No se encontró referer, redirigiendo a: {}", redirectUrl);
            }
        }
        
        return "redirect:" + redirectUrl;
    }
}