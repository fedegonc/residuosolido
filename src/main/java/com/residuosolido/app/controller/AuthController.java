package com.residuosolido.app.controller;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.dto.RegistrationForm;
import com.residuosolido.app.config.RateLimiter;
import org.springframework.dao.DuplicateKeyException;
import com.residuosolido.app.service.UserRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/** Controller de autenticación: registro y login. La landing vive en PageController. */
@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRegistrationService userRegistrationService;
    private final RateLimiter rateLimiter;
    private final Messages messages;

    public AuthController(UserRegistrationService userRegistrationService, RateLimiter rateLimiter, Messages messages) {
        this.userRegistrationService = userRegistrationService;
        this.rateLimiter = rateLimiter;
        this.messages = messages;
    }

    /** Muestra el formulario de registro (ciudadano u organización). */
    @GetMapping(Routes.REGISTER)
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationForm());
        return "auth/register";
    }

    /** Procesa el registro de un nuevo usuario. */
    @PostMapping(Routes.REGISTER)
    public String registerUser(@ModelAttribute("user") RegistrationForm form,
                               @RequestParam(defaultValue = "false") boolean isOrganization,
                               Model model, HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        try {
            if (!rateLimiter.isAllowed(request, "registration")) {
                throw new ValidationException(ServerMessage.FLASH_REQUEST_RATE_LIMITED);
            }
            userRegistrationService.registerUser(form.toUser(), isOrganization);
            messages.flashSuccess(redirectAttributes, ServerMessage.AUTH_LOGIN_SUCCESS);
            return "redirect:/entrar";
        } catch (DuplicateKeyException e) {
            model.addAttribute("errorMessage", messages.msg(ServerMessage.ERROR_REGISTER_IDENTITY_EXISTS));
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", messages.msg(e));
        }
        form.setPassword(null);
        return "auth/register";
    }

    /** Muestra la página de login. Soporta params ?error y ?blocked. */
    @GetMapping(Routes.LOGIN)
    public String showLoginPage(HttpServletRequest request, Model model) {
        if (request.getParameter("blocked") != null) {
            model.addAttribute("errorMessage", messages.msg(ServerMessage.AUTH_LOGIN_BLOCKED));
        } else if (request.getParameter("error") != null) {
            model.addAttribute("errorMessage", messages.msg(ServerMessage.AUTH_LOGIN_ERROR));
        }
        return "auth/login";
    }
}
