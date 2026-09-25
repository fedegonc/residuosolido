package com.residuosolido.app.controller;

import com.residuosolido.app.config.Routes;
import com.residuosolido.app.exception.ServerMessage;
import com.residuosolido.app.exception.ValidationException;
import com.residuosolido.app.exception.StateException;
import com.residuosolido.app.exception.OwnershipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Messages messages;

    public GlobalExceptionHandler(Messages messages) {
        this.messages = messages;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNotFound(HttpServletRequest request) {
        logger.debug("Recurso no encontrado: {}", request.getRequestURI());
        return "error/404";
    }

    /**
     * Sin este handler, ResponseStatusException(NOT_FOUND) (ej. PageController
     * con un slug sin template) caía en el catch-all de Exception y redirigía
     * a /entrar en vez de mostrar un 404 real — bug detectado por
     * LandingCardsTest.RenderingLayer#unknownSlug_returns404.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatus(ResponseStatusException e, HttpServletResponse response) {
        logger.debug("ResponseStatusException: {} {}", e.getStatusCode(), e.getReason());
        response.setStatus(e.getStatusCode().value());
        return "error/404";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.warn("Acceso denegado a: {}", request.getRequestURI());
        redirectAttributes.addFlashAttribute("errorMessage", messages.msg(ServerMessage.FLASH_ERROR_ACCESS_DENIED));
        return redirectOrError(request);
    }

    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(HttpServletRequest request, ValidationException e,
                                           RedirectAttributes redirectAttributes) {
        logger.warn("Validación fallida en {}: {}", request.getRequestURI(), e.key());
        redirectAttributes.addFlashAttribute("errorMessage", messages.msg(e.key()));
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return redirectOrError(request);
    }

    @ExceptionHandler(StateException.class)
    public String handleStateException(HttpServletRequest request, StateException e,
                                      RedirectAttributes redirectAttributes) {
        logger.warn("Error de estado en {}: {}", request.getRequestURI(), e.key());
        redirectAttributes.addFlashAttribute("errorMessage", messages.msg(e.key()));
        return "redirect:" + Routes.REQUESTS;
    }

    @ExceptionHandler(OwnershipException.class)
    public String handleOwnershipException(HttpServletRequest request, OwnershipException e,
                                          RedirectAttributes redirectAttributes) {
        logger.warn("Acceso denegado (propiedad) en {}: {}", request.getRequestURI(), e.key());
        redirectAttributes.addFlashAttribute("errorMessage", messages.msg(e.key()));
        return "redirect:" + Routes.REQUESTS;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public String handleOptimisticLocking(HttpServletRequest request, OptimisticLockingFailureException e,
                                         RedirectAttributes redirectAttributes) {
        logger.warn("Conflicto de concurrencia en {}", request.getRequestURI());
        redirectAttributes.addFlashAttribute("warningMessage", messages.msg(ServerMessage.FLASH_REQUEST_CONCURRENT_MODIFICATION));
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return redirectOrError(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(HttpServletRequest request, IllegalArgumentException e,
                                        RedirectAttributes redirectAttributes) {
        logger.warn("Argumento inválido en {}: {}", request.getRequestURI(), e.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", messages.msg(e));
        // Un error de validación (ej. teléfono mal formado) debe devolver al formulario
        // donde ocurrió, no a un destino "genérico" por rol — si no, el usuario ve el
        // error en una pantalla sin el campo que lo causó (ej. termina en /entrar).
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return redirectOrError(request);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(HttpServletRequest request, Exception e, RedirectAttributes redirectAttributes) {
        logger.error("Error no manejado en {}: {}", request.getRequestURI(), e.getMessage(), e);
        redirectAttributes.addFlashAttribute("errorMessage", messages.msg(ServerMessage.FLASH_ERROR_GENERIC));
        return redirectOrError(request);
    }

    private String redirectOrError(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return Routes.resolveErrorNavigation(auth, request.getRequestURI());
    }
}
