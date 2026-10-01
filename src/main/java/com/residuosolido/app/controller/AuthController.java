package com.residuosolido.app.controller;

import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;

import com.residuosolido.app.config.Routes;

import com.residuosolido.app.dto.RegistrationForm;
import com.residuosolido.app.enums.OrgType;
import com.residuosolido.app.model.User;
import com.residuosolido.app.config.RateLimiter;
import org.springframework.dao.DuplicateKeyException;
import com.residuosolido.app.service.UserRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/** Controller de autenticación: registro y login. La landing vive en PageController. */
@Controller
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRegistrationService userRegistrationService;
    private final RateLimiter rateLimiter;
    private final Messages messages;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRegistrationService userRegistrationService, RateLimiter rateLimiter, Messages messages,
                          AuthenticationManager authenticationManager) {
        this.userRegistrationService = userRegistrationService;
        this.rateLimiter = rateLimiter;
        this.messages = messages;
        this.authenticationManager = authenticationManager;
    }

    /** Formulario de registro de ciudadano. */
    @GetMapping(Routes.REGISTER)
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationForm());
        return "auth/register";
    }

    /** Formulario de registro de organización — pide el tipo desde el inicio. */
    @GetMapping(Routes.REGISTER_ORG)
    public String showOrgRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationForm());
        model.addAttribute("orgTypes", OrgType.values());
        return "auth/register-org";
    }

    /** Procesa el registro de un ciudadano. */
    @PostMapping(Routes.REGISTER)
    public String registerCitizen(@Valid @ModelAttribute("user") RegistrationForm form, BindingResult bindingResult,
                                  Model model, HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) {
        return doRegister(form, bindingResult, model, request, redirectAttributes,
                u -> userRegistrationService.registerCitizen(u), "auth/register");
    }

    /** Procesa el registro de una organización (tipo obligatorio). */
    @PostMapping(Routes.REGISTER_ORG)
    public String registerOrganization(@Valid @ModelAttribute("user") RegistrationForm form, BindingResult bindingResult,
                                       Model model, HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        model.addAttribute("orgTypes", OrgType.values());
        if (form.getTipo() == null) {
            model.addAttribute("errorMessage", messages.msg(ServerMessage.ERROR_REGISTER_ORG_TYPE_REQUIRED));
            form.setPassword(null);
            return "auth/register-org";
        }
        return doRegister(form, bindingResult, model, request, redirectAttributes,
                u -> userRegistrationService.registerOrganization(u, form.getTipo()), "auth/register-org");
    }

    private String doRegister(RegistrationForm form, BindingResult bindingResult,
                              Model model, HttpServletRequest request,
                              RedirectAttributes redirectAttributes,
                              java.util.function.Function<User, User> register, String view) {
        // Bean Validation cubre forma (username/PIN) y falla más rápido que antes de
        // tocar el repositorio — no reemplaza a UserRegistrationService.validateUserRegistration,
        // que sigue siendo la fuente de verdad para teléfono (compuesto) y unicidad
        // de username (necesita el repo). Ver comentario en RegistrationForm.
        if (bindingResult.hasFieldErrors()) {
            model.addAttribute("errorMessage", messages.msg(bindingResult.getFieldError().getDefaultMessage()));
            form.setPassword(null);
            return view;
        }
        String rawPin = form.getPassword();
        try {
            if (!rateLimiter.isAllowed(request, "registration")) {
                throw new ValidationException(ServerMessage.FLASH_REQUEST_RATE_LIMITED);
            }
            User created = register.apply(form.toUser());
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(created.getUsername(), rawPin));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            request.getSession(true);
            request.changeSessionId();
            request.getSession().setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
            messages.flashSuccess(redirectAttributes, ServerMessage.AUTH_LOGIN_SUCCESS);
            return "redirect:" + Routes.resolveHomeForRole(authentication);
        } catch (DuplicateKeyException e) {
            bindingResult.rejectValue("username", "error.register.identity_exists",
                    messages.msg(ServerMessage.ERROR_REGISTER_IDENTITY_EXISTS));
            model.addAttribute("errorMessage", messages.msg(ServerMessage.ERROR_REGISTER_IDENTITY_EXISTS));
        } catch (ValidationException e) {
            String field = registrationField(e.key());
            if (field != null) bindingResult.rejectValue(field, e.key().code(), messages.msg(e));
            model.addAttribute("errorMessage", messages.msg(e));
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", messages.msg(e));
        }
        form.setPassword(null);
        return view;
    }

    private String registrationField(ServerMessage key) {
        return switch (key) {
            case ERROR_REGISTER_USERNAME_REQUIRED, ERROR_REGISTER_USERNAME_TOO_LONG,
                 ERROR_REGISTER_USERNAME_EXISTS, ERROR_REGISTER_IDENTITY_EXISTS -> "username";
            case ERROR_REGISTER_PIN_INVALID -> "password";
            case ERROR_REGISTER_PHONE_REQUIRED, ERROR_PHONE_REQUIRED, ERROR_PHONE_INVALID,
                 ERROR_PHONE_INVALID_LENGTH, ERROR_PHONE_INVALID_FIRST_DIGIT,
                 ERROR_PHONE_INVALID_DDD, ERROR_PHONE_UNSUPPORTED_COUNTRY -> "phoneNational";
            case ERROR_REGISTER_ORG_TYPE_REQUIRED -> "tipo";
            default -> null;
        };
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
