package com.residuosolido.app.controller;

import com.residuosolido.app.model.User;
import com.residuosolido.app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final MessageSource messageSource;

    @Autowired
    public AuthController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
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
                               Model model) {
        String validationError = userService.validateUserRegistration(user);
        if (validationError != null) {
            model.addAttribute("error", messageSource.getMessage(validationError, null, LocaleContextHolder.getLocale()));
            return "auth/register";
        }

        userService.registerUser(user, isOrganization);
        return "redirect:/auth/login?success";
    }

    @GetMapping("/auth/login")
    public String showLoginPage() {
        return "auth/login";
    }

    @GetMapping({"/", "/index"})
    public String rootOrIndex() {
        return "index";
    }
}
