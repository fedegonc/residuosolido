package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AuthController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRegistrationService userRegistrationService;

    @Autowired
    public AuthController(UserRegistrationService userRegistrationService) {
        this.userRegistrationService = userRegistrationService;
    }

    @GetMapping("/auth/register")
    public String showRegistrationForm(@AuthenticationPrincipal UserDetails userDetails,
                                     HttpServletResponse response,
                                     Model model) throws IOException {
        if (userDetails != null) {
            response.sendRedirect("/");
            return null;
        }

        model.addAttribute("user", new User());
        return "auth/register";
    }

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

    @GetMapping("/auth/login")
    public String showLoginPage(HttpServletRequest request, Model model) {
        if (request.getParameter("blocked") != null) {
            model.addAttribute("errorMessage", msg("login.blocked"));
        } else if (request.getParameter("error") != null) {
            model.addAttribute("errorMessage", msg("login.error"));
        }
        return "auth/login";
    }

    @GetMapping({"/", "/index"})
    public String rootOrIndex() {
        return "public/index";
    }
}
