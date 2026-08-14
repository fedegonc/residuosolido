package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/** Controller de autenticación: registro, login y página de inicio pública. */
@Controller
public class AuthController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRegistrationService userRegistrationService;

    @Autowired
    public AuthController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    /** Muestra el formulario de registro (ciudadano u organización). */
    @GetMapping("/auth/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    /** Procesa el registro de un nuevo usuario. */
    @PostMapping("/auth/register")
    public String registerUser(@ModelAttribute User user,
                               @RequestParam(value = "isOrganization", required = false) String isOrganization,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        String validationError = userRegistrationService.validateUserRegistration(user);
        if (validationError != null) {
            model.addAttribute("errorMessage", msg(validationError));
            return "auth/register";
        }

        userRegistrationService.registerUser(user, isOrganization);
        flashSuccess(redirectAttributes, "login.success");
        return "redirect:/auth/login";
    }

    /** Muestra la página de login. Soporta params ?error y ?blocked. */
    @GetMapping("/auth/login")
    public String showLoginPage(HttpServletRequest request, Model model) {
        if (request.getParameter("blocked") != null) {
            model.addAttribute("errorMessage", msg("login.blocked"));
        } else if (request.getParameter("error") != null) {
            model.addAttribute("errorMessage", msg("login.error"));
        }
        return "auth/login";
    }

    /** Página de inicio pública (landing page). */
    @GetMapping({"/", "/index"})
    public String rootOrIndex() {
        return "public/index";
    }
}
