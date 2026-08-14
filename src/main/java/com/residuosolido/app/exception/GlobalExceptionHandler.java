package com.residuosolido.app.exception;

import com.residuosolido.app.config.RoleBasedLoginTargetUrlResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RoleBasedLoginTargetUrlResolver targetUrlResolver;

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(HttpServletRequest request) {
        logger.debug("Recurso no encontrado: {}", request.getRequestURI());
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.warn("Acceso denegado a: {}", request.getRequestURI());
        redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.error.access_denied", null, LocaleContextHolder.getLocale()));
        return "redirect:" + resolveTarget();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(HttpServletRequest request, IllegalArgumentException e,
                                        RedirectAttributes redirectAttributes) {
        logger.warn("Argumento inválido en {}: {}", request.getRequestURI(), e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale()));
        return "redirect:" + resolveTarget();
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(HttpServletRequest request, Exception e, RedirectAttributes redirectAttributes) {
        logger.error("Error no manejado en {}: {}", request.getRequestURI(), e.getMessage(), e);
        redirectAttributes.addFlashAttribute("errorMessage", messageSource.getMessage("flash.error.generic", null, LocaleContextHolder.getLocale()));
        return "redirect:" + resolveTarget();
    }

    private String resolveTarget() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String url = targetUrlResolver.resolveTargetUrl(auth);
        return url.equals("/") ? "/auth/login" : url;
    }
}
