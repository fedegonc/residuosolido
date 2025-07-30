package com.residuosolido.app.controller;

import com.residuosolido.app.config.CustomAuthenticationSuccessHandler;
import com.residuosolido.app.model.Post;
import com.residuosolido.app.model.User;
import com.residuosolido.app.model.WasteSection;
import com.residuosolido.app.model.Role;
import com.residuosolido.app.repository.UserRepository;
import com.residuosolido.app.service.DashboardService;
import com.residuosolido.app.service.PasswordResetService;
import com.residuosolido.app.service.PostService;
import com.residuosolido.app.service.ConfigService;
import com.residuosolido.app.service.WasteSectionService;
import com.residuosolido.app.service.AuthService;
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
import java.util.Map;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final DashboardService dashboardService;
    private final PasswordResetService passwordResetService;
    private final PostService postService;
    private final ConfigService configService;
    private final WasteSectionService wasteSectionService;
    private final AuthService authService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                         CustomAuthenticationSuccessHandler successHandler,
                         DashboardService dashboardService, PasswordResetService passwordResetService,
                         PostService postService, ConfigService configService, WasteSectionService wasteSectionService,
                         AuthService authService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.successHandler = successHandler;
        this.dashboardService = dashboardService;
        this.passwordResetService = passwordResetService;
        this.postService = postService;
        this.configService = configService;
        this.wasteSectionService = wasteSectionService;
        this.authService = authService;
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

    // Cargar datos del index usando el servicio
    Map<String, Object> indexData = authService.getIndexData();
    model.addAllAttributes(indexData);

    return "guest/index";
}

@GetMapping("/invitados")
public String invitados(Model model) {
    Map<String, Object> invitadosData = authService.getInvitadosData();
    model.addAllAttributes(invitadosData);
    
    return "guest/invitados";
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
    public String registerUser(@ModelAttribute User user, Model model) {
        
        // Validar usuario usando el servicio
        String validationError = authService.validateUserRegistration(user);
        if (validationError != null) {
            model.addAttribute("error", validationError);
            return "auth/register";
        }
        
        // Registrar usuario usando el servicio
        authService.registerUser(user);
        
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
